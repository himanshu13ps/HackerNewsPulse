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
     * Retrieves cached stories for a specific story type and page
     * @param storyType The type of stories to retrieve
     * @param page The page number (1-based)
     * @param pageSize The number of items per page
     * @return List of cached stories or null if not cached
     */
    suspend fun getCachedStories(storyType: StoryType, page: Int, pageSize: Int): List<StoryResponse>?
    
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
     * Returns current cache statistics
     */
    fun getCacheStats(): CacheStats
}