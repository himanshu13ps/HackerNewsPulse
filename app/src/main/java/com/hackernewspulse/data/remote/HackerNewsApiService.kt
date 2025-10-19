package com.hackernewspulse.data.remote

import com.hackernewspulse.data.remote.responses.StoryResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface HackerNewsApiService {

    @GET("v0/topstories.json")
    suspend fun getTopStoryIds(): List<Long>

    @GET("v0/newstories.json")
    suspend fun getNewStoryIds(): List<Long>

    @GET("v0/item/{id}.json")
    suspend fun getStory(@Path("id") id: Long): StoryResponse
}