package com.hackernewspulse.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hackernewspulse.data.cache.StoryCacheManager
import com.hackernewspulse.data.remote.HackerNewsApiService
import com.hackernewspulse.data.remote.responses.StoryResponse

/**
 * Enhanced paging source that implements a hybrid loading strategy:
 * - Page 1: Returns cached hydrated stories if preloaded, or progressive card-by-card slots
 *   for immediate first render while kicking off a 2-page bulk lookahead prefetch.
 * - Pages 2+: Serves pre-filtered, hydrated stories in bulk from cache or bulk network fetch,
 *   while maintaining a 2-page lookahead prefetch window for smooth scrolling.
 */
class CachedHackerNewsPagingSource(
    private val apiService: HackerNewsApiService,
    private val storyType: StoryType,
    private val cacheManager: StoryCacheManager
) : PagingSource<Int, StoryResponse>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryResponse> {
        return try {
            val page = params.key ?: 1
            val pageSize = params.loadSize

            if (page == 1) {
                // First, check if Page 1 was already fully preloaded in cache
                val cachedStories = cacheManager.getCachedStories(storyType, page, pageSize)
                println("HackerNewsPulseLog: Cache check for $storyType page 1: ${cachedStories?.size ?: 0} items found")
                if (!cachedStories.isNullOrEmpty()) {
                    // Proactively prefetch Pages 2 and 3 in bulk
                    cacheManager.prefetchNextPages(storyType, startPage = 2, pageSize = pageSize, pagesToPrefetch = 2)
                    return LoadResult.Page(
                        data = cachedStories,
                        prevKey = null,
                        nextKey = if (cachedStories.size < pageSize) null else 2
                    )
                }

                // Page 1 cache miss: prepare progressive Page 1 slots (which also starts bulk prefetch for Pages 2 & 3)
                val pageOneSlots = cacheManager.preparePageOneSlots(storyType, pageSize)
                println("HackerNewsPulseLog: Prepared ${pageOneSlots.size} progressive slots for $storyType page 1")
                return LoadResult.Page(
                    data = pageOneSlots,
                    prevKey = null,
                    nextKey = if (pageOneSlots.size < pageSize) null else 2
                )
            } else {
                // Pages 2+: Always serve hydrated, URL-validated stories in bulk (from lookahead cache or bulk fetch)
                val bulkStories = cacheManager.getOrFetchBulkPage(storyType, page, pageSize)
                println("HackerNewsPulseLog: Loaded bulk page $page for $storyType: ${bulkStories.size} items")
                return LoadResult.Page(
                    data = bulkStories,
                    prevKey = page - 1,
                    nextKey = if (bulkStories.size < pageSize) null else page + 1
                )
            }
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, StoryResponse>): Int? {
        return null
    }
}