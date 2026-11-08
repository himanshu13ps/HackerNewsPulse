package com.hackernewspulse.data.cache

import com.hackernewspulse.data.paging.StoryType
import com.hackernewspulse.data.remote.responses.StoryResponse

/**
 * Interface for managing story cache operations
 */
interface StoryCacheManager {
    
    /**
     * Preloads cache with first page of both TOP and NEW stories
     */
    suspend fun preloadCache()

    /**
     * Retrieves cached story IDs for a story type, or fetches and caches them from the network
     */
    suspend fun getOrFetchStoryIds(storyType: StoryType): List<Long>
    
    /**
     * Retrieves cached stories for a specific story type and page
     * @param storyType The type of stories to retrieve
     * @param page The page number (1-based)
     * @param pageSize The number of items per page
     * @return List of cached stories or null if not cached
     */
    suspend fun getCachedStories(storyType: StoryType, page: Int, pageSize: Int): List<StoryResponse>?

    /**
     * Prepares Page 1 progressive slots for card-by-card rendering when Page 1 is not yet fully cached,
     * and initiates background prefetching for subsequent pages.
     */
    suspend fun preparePageOneSlots(storyType: StoryType, pageSize: Int): List<StoryResponse>

    /**
     * Resolves a story for a given ID (or Page 1 slot), automatically replacing URL-less or failed
     * stories with the next valid URL-bearing story from the reserve pool so no empty gaps occur.
     */
    suspend fun resolveStoryForCard(storyType: StoryType, id: Long): StoryResponse?

    /**
     * Gets or bulk-fetches a full page of valid URL-bearing stories (used for Pages 2+)
     * and triggers background lookahead prefetching for subsequent pages.
     */
    suspend fun getOrFetchBulkPage(storyType: StoryType, page: Int, pageSize: Int): List<StoryResponse>

    /**
     * Triggers background bulk prefetching for upcoming pages
     */
    fun prefetchNextPages(storyType: StoryType, startPage: Int, pageSize: Int, pagesToPrefetch: Int = 2)
    
    /**
     * Adds stories to cache for a specific story type and page
     * @param storyType The type of stories to cache
     * @param stories The stories to add to cache
     * @param page The page number (1-based)
     */
    suspend fun addStoriesToCache(storyType: StoryType, stories: List<StoryResponse>, page: Int)
    
    /**
     * Invalidates all cached data for a specific story type
     * @param storyType The story type to invalidate
     */
    suspend fun invalidateCache(storyType: StoryType)
    
    /**
     * Checks if there is cached data available for a story type
     * @param storyType The story type to check
     * @return true if cached data exists, false otherwise
     */
    suspend fun hasCachedData(storyType: StoryType): Boolean
    
    /**
     * Retrieves a single cached story by its ID
     */
    suspend fun getStoryById(id: Long): StoryResponse?

    /**
     * Adds a single story to the detail cache
     */
    suspend fun saveStory(story: StoryResponse)

    /**
     * Returns current cache statistics
     */
    fun getCacheStats(): CacheStats
}