package com.hackernewspulse

import android.app.Application
import com.hackernewspulse.data.cache.CachePreloader
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HackerNewsPulseApplication : Application() {
    
    @Inject
    lateinit var cachePreloader: CachePreloader
    
    override fun onCreate() {
        super.onCreate()
        
        // Start cache preloading as early as possible in app startup
        cachePreloader.startPreloading()
    }
}