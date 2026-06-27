package com.hackernewspulse.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.hackernewspulse.data.cache.StoryCacheManager
import com.hackernewspulse.data.paging.CachedHackerNewsPagingSource
import com.hackernewspulse.data.paging.StoryType
import com.hackernewspulse.data.remote.HackerNewsApiService
import com.hackernewspulse.data.remote.responses.StoryResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HackerNewsRepository @Inject constructor(
    private val apiService: HackerNewsApiService,
    private val cacheManager: StoryCacheManager
) {
    fun getStories(storyType: StoryType): Flow<PagingData<StoryResponse>> {
        return Pager(
            config = PagingConfig(
                pageSize = 10,
                initialLoadSize = 10,  // Control initial load size to prevent loading 60 items
                enablePlaceholders = true
            ),
            pagingSourceFactory = { CachedHackerNewsPagingSource(apiService, storyType, cacheManager) }
        ).flow
    }

    /**
     * Fetches a single story by ID, checking the cache first
     */
    suspend fun getStory(id: Long): StoryResponse {
        // Try cache first
        cacheManager.getStoryById(id)?.let { return it }
        
        // Fetch from network
        val story = apiService.getStory(id)
        
        // Save to cache
        cacheManager.saveStory(story)
        
        return story
    }
    
    /**
     * Invalidates cache for the specified story type
     * Used for pull-to-refresh functionality
     */
    suspend fun invalidateCache(storyType: StoryType) {
        cacheManager.invalidateCache(storyType)
    }
    
    /**
     * Returns current cache statistics
     */
    fun getCacheStats() = cacheManager.getCacheStats()
}
