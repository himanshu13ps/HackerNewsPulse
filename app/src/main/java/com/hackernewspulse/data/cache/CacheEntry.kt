package com.hackernewspulse.data.cache

import com.hackernewspulse.data.paging.StoryType
import com.hackernewspulse.data.remote.responses.StoryResponse

/**
 * Represents a cache entry containing stories for a specific page and story type
 */
data class CacheEntry(
    val stories: List<StoryResponse>,
    val page: Int,
    val timestamp: Long,
    val storyType: StoryType
)