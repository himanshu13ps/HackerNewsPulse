package com.hackernewspulse.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.hackernewspulse.data.paging.HackerNewsPagingSource
import com.hackernewspulse.data.paging.StoryType
import com.hackernewspulse.data.remote.HackerNewsApiService
import com.hackernewspulse.data.remote.responses.StoryResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HackerNewsRepository @Inject constructor(
    private val apiService: HackerNewsApiService
) {
    fun getStories(storyType: StoryType): Flow<PagingData<StoryResponse>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { HackerNewsPagingSource(apiService, storyType) }
        ).flow
    }
}
