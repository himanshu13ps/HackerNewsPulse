package com.hackernewspulse.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.hackernewspulse.data.remote.HackerNewsApiService
import com.hackernewspulse.data.remote.responses.StoryResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

enum class StoryType { TOP, NEW }

class HackerNewsPagingSource(
    private val apiService: HackerNewsApiService,
    private val storyType: StoryType
) : PagingSource<Int, StoryResponse>() {

    private var storyIds: List<Long> = emptyList()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StoryResponse> {
        return try {
            val page = params.key ?: 1
            val pageSize = params.loadSize

            if (storyIds.isEmpty()) {
                storyIds = when (storyType) {
                    StoryType.TOP -> apiService.getTopStoryIds()
                    StoryType.NEW -> apiService.getNewStoryIds()
                }
            }

            val fromIndex = (page - 1) * pageSize
            if (fromIndex >= storyIds.size) {
                return LoadResult.Page(emptyList(), prevKey = null, nextKey = null)
            }
            val toIndex = minOf(fromIndex + pageSize, storyIds.size)
            val idsToFetch = storyIds.subList(fromIndex, toIndex)

            val stories = coroutineScope {
                idsToFetch.map { id ->
                    async { apiService.getStory(id) }
                }.awaitAll()
            }

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
