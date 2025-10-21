package com.hackernewspulse.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hackernewspulse.data.cache.StoryCacheManager
import com.hackernewspulse.data.remote.HackerNewsApiService
import com.hackernewspulse.data.remote.responses.StoryResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Enhanced paging source that implements cache-first loading strategy
 */
class CachedHackerNewsPagingSource(
    private val apiService: HackerNewsApiService,
    private val storyType: StoryType,
    private val cacheManager: StoryCacheManager
) : PagingSource<Int, StoryResponse>() {

    private var storyIds: List<Long> = emptyList()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryResponse> {
        return try {
            val page = params.key ?: 1
            val pageSize = params.loadSize

            // First, try to get data from cache
            val cachedStories = cacheManager.getCachedStories(storyType, page, pageSize)
            println("HackerNewsPulseLog: Cache check for $storyType page $page: ${cachedStories?.size ?: 0} items found")
            if (cachedStories != null && cachedStories.isNotEmpty()) {
                // Return cached data immediately for fast display
                return LoadResult.Page(
                    data = cachedStories,
                    prevKey = if (page == 1) null else page - 1,
                    nextKey = if (cachedStories.size < pageSize) null else page + 1
                )
            }

            // Cache miss - fetch from network
            if (storyIds.isEmpty()) {
                storyIds = when (storyType) {
                    StoryType.TOP -> apiService.getTopStoryIds()
                    StoryType.NEW -> apiService.getNewStoryIds()
                }
            }

            // Standard pagination logic
            val fromIndex = (page - 1) * pageSize
            if (fromIndex >= storyIds.size) {
                return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
            }
            val toIndex = minOf(fromIndex + pageSize, storyIds.size)
            val idsToFetch = storyIds.subList(fromIndex, toIndex)
            println("HackerNewsPulseLog: Loading from network for $storyType page $page: ${idsToFetch.size} items (indices $fromIndex-$toIndex)")

            val stories = coroutineScope {
                idsToFetch.map { id ->
                    async { apiService.getStory(id) }
                }.awaitAll()
            }

            // Update cache with newly loaded data
            cacheManager.addStoriesToCache(storyType, stories, page)

            LoadResult.Page(
                data = stories,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (toIndex >= storyIds.size) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, StoryResponse>): Int? {
        return null
    }
}