package com.hackernewspulse.di

import com.hackernewspulse.data.cache.StoryCacheManager
import com.hackernewspulse.data.cache.StoryCacheManagerImpl
import com.hackernewspulse.data.remote.HackerNewsApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private val json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://hacker-news.firebaseio.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideHackerNewsApiService(retrofit: Retrofit): HackerNewsApiService {
        return retrofit.create(HackerNewsApiService::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CacheModule {

    @Binds
    @Singleton
    abstract fun bindStoryCacheManager(
        storyCacheManagerImpl: StoryCacheManagerImpl
    ): StoryCacheManager
}
