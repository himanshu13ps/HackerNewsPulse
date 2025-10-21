package com.hackernewspulse.data.cache

/**
 * Statistics for cache performance monitoring
 */
data class CacheStats(
    val topStoriesHits: Int = 0,
    val topStoriesMisses: Int = 0,
    val newStoriesHits: Int = 0,
    val newStoriesMisses: Int = 0,
    val lastPreloadTime: Long? = null
) {
    val totalHits: Int get() = topStoriesHits + newStoriesHits
    val totalMisses: Int get() = topStoriesMisses + newStoriesMisses
    val hitRatio: Float get() = if (totalHits + totalMisses > 0) totalHits.toFloat() / (totalHits + totalMisses) else 0f
}