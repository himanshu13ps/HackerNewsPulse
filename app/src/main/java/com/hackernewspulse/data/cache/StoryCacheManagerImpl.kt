package com.hackernewspulse.data.cache

import com.hackernewspulse.data.paging.StoryType
import com.hackernewspulse.data.remote.HackerNewsApiService
import com.hackernewspulse.data.remote.responses.StoryResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of StoryCacheManager with thread-safe in-memory storage
 */
@Singleton
class StoryCacheManagerImpl @Inject constructor(
    private val apiService: HackerNewsApiService
) : StoryCacheManager {
    
    // Thread-safe cache storage: StoryType -> Page -> List<StoryResponse>
    private val cache = ConcurrentHashMap<StoryType, ConcurrentHashMap<Int, List<StoryResponse>>>()
    
    // Mutex for write operations to ensure thread safety
    private val writeMutex = Mutex()
    
    // Cache statistics tracking
    private var stats = CacheStats()
    private val statsMutex = Mutex()
    
    companion object {
        private const val DEFAULT_PAGE_SIZE = 10
        private const val FIRST_PAGE = 1
    }
    
    override suspend fun preloadCache() {
        try {
            coroutineScope {
                // Preload both story types concurrently
                val topStoriesDeferred = async { preloadStoryType(StoryType.TOP) }
                val newStoriesDeferred = async { preloadStoryType(StoryType.NEW) }
                
                // Wait for both to complete
                topStoriesDeferred.await()
                newStoriesDeferred.await()
            }
            
            // Update preload timestamp
            statsMutex.withLock {
                stats = stats.copy(lastPreloadTime = System.currentTimeMillis())
            }
        } catch (e: Exception) {
            // Log error but don't throw to avoid blocking app startup
            // In a real app, you would use proper logging here
            println("HackerNewsPulseLog: Cache preload failed: ${e.message}")
        }
    }
    
    private suspend fun preloadStoryType(storyType: StoryType) {
        try {
            // Get story IDs for the story type
            val storyIds = when (storyType) {
                StoryType.TOP -> apiService.getTopStoryIds()
                StoryType.NEW -> apiService.getNewStoryIds()
            }
            
            if (storyIds.isNotEmpty()) {
                // Get first page of stories (20 items)
                val preloadIds = storyIds.take(DEFAULT_PAGE_SIZE)
                println("HackerNewsPulseLog: Preloading $storyType: ${preloadIds.size} items")
                val stories = coroutineScope {
                    preloadIds.map { id ->
                        async { apiService.getStory(id) }
                    }.awaitAll()
                }
                
                // Add to cache
                addStoriesToCache(storyType, stories, FIRST_PAGE)
                println("HackerNewsPulseLog: Cached $storyType: ${stories.size} items")
            }
        } catch (e: Exception) {
            // Log error but continue with other story type
            println("HackerNewsPulseLog: Failed to preload $storyType stories: ${e.message}")
        }
    }
    
    override suspend fun getCachedStories(storyType: StoryType, page: Int, pageSize: Int): List<StoryResponse>? {
        val typeCache = cache[storyType] ?: return null.also { recordCacheMiss(storyType) }
        
        val cachedPage = typeCache[page]
        return if (cachedPage != null) {
            recordCacheHit(storyType)
            // Return the requested page size, or all available if less than requested
            cachedPage.take(pageSize)
        } else {
            recordCacheMiss(storyType)
            null
        }
    }
    
    override suspend fun addStoriesToCache(storyType: StoryType, stories: List<StoryResponse>, page: Int) {
        writeMutex.withLock {
            val typeCache = cache.getOrPut(storyType) { ConcurrentHashMap() }
            typeCache[page] = stories
        }
    }
    
    override suspend fun invalidateCache(storyType: StoryType) {
        writeMutex.withLock {
            cache[storyType]?.clear()
        }
    }
    
    override suspend fun hasCachedData(storyType: StoryType): Boolean {
        val typeCache = cache[storyType] ?: return false
        return typeCache.isNotEmpty()
    }
    
    override fun getCacheStats(): CacheStats {
        return stats
    }
    
    private suspend fun recordCacheHit(storyType: StoryType) {
        statsMutex.withLock {
            stats = when (storyType) {
                StoryType.TOP -> stats.copy(topStoriesHits = stats.topStoriesHits + 1)
                StoryType.NEW -> stats.copy(newStoriesHits = stats.newStoriesHits + 1)
            }
        }
    }
    
    private suspend fun recordCacheMiss(storyType: StoryType) {
        statsMutex.withLock {
            stats = when (storyType) {
                StoryType.TOP -> stats.copy(topStoriesMisses = stats.topStoriesMisses + 1)
                StoryType.NEW -> stats.copy(newStoriesMisses = stats.newStoriesMisses + 1)
            }
        }
    }
}