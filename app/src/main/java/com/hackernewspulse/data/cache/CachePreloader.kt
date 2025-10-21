package com.hackernewspulse.data.cache

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Component responsible for preloading cache during app startup
 */
@Singleton
class CachePreloader @Inject constructor(
    private val cacheManager: StoryCacheManager
) {
    
    // Use a supervisor job to ensure one failure doesn't cancel the other
    private val preloadScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    /**
     * Initiates cache preloading for both story types
     * This method is non-blocking and handles failures gracefully
     */
    fun startPreloading() {
        preloadScope.launch {
            try {
                cacheManager.preloadCache()
            } catch (e: Exception) {
                // Log error but don't crash the app
                // In a real app, you would use proper logging here
                println("HackerNewsPulseLog: Cache preloading failed: ${e.message}")
            }
        }
    }
    
    /**
     * Preloads cache synchronously - use only when you need to wait for completion
     * This is primarily for testing or specific initialization scenarios
     */
    suspend fun preloadCacheSync() {
        try {
            cacheManager.preloadCache()
        } catch (e: Exception) {
            // Log error but don't throw to avoid blocking caller
            println("HackerNewsPulseLog: Synchronous cache preloading failed: ${e.message}")
        }
    }
}