package com.hackernewspulse.data.cache

import com.hackernewspulse.data.paging.StoryType
import com.hackernewspulse.data.remote.HackerNewsApiService
import com.hackernewspulse.data.remote.responses.StoryResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Concrete implementation of StoryCacheManager with thread-safe in-memory storage,
 * in-flight request deduplication, progressive Page 1 slot resolution, and bulk multi-page prefetching.
 */
@Singleton
class StoryCacheManagerImpl @Inject constructor(
    private val apiService: HackerNewsApiService
) : StoryCacheManager {

    private val backgroundScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Thread-safe cache storage: StoryType -> Page -> List<StoryResponse>
    private val cache = ConcurrentHashMap<StoryType, ConcurrentHashMap<Int, List<StoryResponse>>>()

    // Cached story ID lists per StoryType
    private val storyIdsCache = ConcurrentHashMap<StoryType, List<Long>>()
    private val inFlightStoryIds = ConcurrentHashMap<StoryType, Deferred<List<Long>>>()
    private val idsMutex = Mutex()

    // Flat cache for individual story details and in-flight request deduplication
    private val storyDetailCache = ConcurrentHashMap<Long, StoryResponse>()
    private val inFlightStories = ConcurrentHashMap<Long, Deferred<StoryResponse?>>()
    private val storyFetchMutex = Mutex()

    // Page 1 progressive slot state per StoryType:
    // Maps initial Page 1 slot ID -> resolved valid StoryResponse (which may have been replaced from reserve)
    private val pageOneResolvedSlots = ConcurrentHashMap<StoryType, ConcurrentHashMap<Long, StoryResponse>>()
    private val pageOneReserveQueues = ConcurrentHashMap<StoryType, ConcurrentLinkedQueue<Long>>()

    // Cursor tracking how far into storyIds we have consumed for bulk pages (Pages 2+) per StoryType
    private val bulkIndexCursor = ConcurrentHashMap<StoryType, AtomicInteger>()
    // Queue of already-fetched valid URL-bearing stories carried over from previous batch over-fetches
    private val bulkOverflowQueues = ConcurrentHashMap<StoryType, ConcurrentLinkedQueue<StoryResponse>>()
    // Per-StoryType mutex to serialize bulk page building so pages 2, 3, 4... never overlap or race
    private val bulkPageMutexes = ConcurrentHashMap<StoryType, Mutex>().apply {
        StoryType.values().forEach { put(it, Mutex()) }
    }

    // Mutex for write operations to ensure thread safety
    private val writeMutex = Mutex()

    // Cache statistics tracking
    private var stats = CacheStats()
    private val statsMutex = Mutex()

    companion object {
        private const val DEFAULT_PAGE_SIZE = 10
        private const val FIRST_PAGE = 1
        // Reserve pool size for Page 1 so URL-less stories (e.g. Ask HN) can be seamlessly replaced
        private const val PAGE_ONE_RESERVE_COUNT = 8
        private const val PAGE_ONE_WINDOW_SIZE = DEFAULT_PAGE_SIZE + PAGE_ONE_RESERVE_COUNT
    }

    override suspend fun preloadCache() {
        try {
            // Prioritize TOP stories first so initial screen render has zero contention with NEW stories
            preloadStoryType(StoryType.TOP)
            // Start prefetching pages 2 & 3 for TOP in the background
            prefetchNextPages(StoryType.TOP, startPage = 2, pageSize = DEFAULT_PAGE_SIZE, pagesToPrefetch = 2)

            // Then preload NEW stories
            preloadStoryType(StoryType.NEW)

            // Update preload timestamp
            statsMutex.withLock {
                stats = stats.copy(lastPreloadTime = System.currentTimeMillis())
            }
        } catch (e: Exception) {
            println("HackerNewsPulseLog: Cache preload failed: ${e.message}")
        }
    }

    private suspend fun preloadStoryType(storyType: StoryType) {
        try {
            val storyIds = getOrFetchStoryIds(storyType)
            if (storyIds.isNotEmpty()) {
                // Warm the first window of story details concurrently using deduplicated requests
                val preloadIds = storyIds.take(PAGE_ONE_WINDOW_SIZE)
                println("HackerNewsPulseLog: Preloading $storyType: ${preloadIds.size} candidate items")
                val validStories = coroutineScope {
                    preloadIds.map { id ->
                        async { fetchStoryDeduplicated(id) }
                    }.awaitAll()
                }.filterNotNull().filter { !it.url.isNullOrBlank() }.take(DEFAULT_PAGE_SIZE)

                if (validStories.isNotEmpty()) {
                    // Only write full Page 1 cache if Page 1 slots aren't already mid-render with different IDs
                    val existingSlots = pageOneResolvedSlots[storyType]
                    if (existingSlots == null || existingSlots.isEmpty()) {
                        addStoriesToCache(storyType, validStories, FIRST_PAGE)
                    }
                    bulkIndexCursor.putIfAbsent(storyType, AtomicInteger(minOf(PAGE_ONE_WINDOW_SIZE, storyIds.size)))
                    println("HackerNewsPulseLog: Cached $storyType page 1: ${validStories.size} valid items")
                }
            }
        } catch (e: Exception) {
            println("HackerNewsPulseLog: Failed to preload $storyType stories: ${e.message}")
        }
    }

    override suspend fun getOrFetchStoryIds(storyType: StoryType): List<Long> {
        storyIdsCache[storyType]?.takeIf { it.isNotEmpty() }?.let { return it }

        val deferred = idsMutex.withLock {
            storyIdsCache[storyType]?.takeIf { it.isNotEmpty() }?.let {
                return@withLock kotlinx.coroutines.CompletableDeferred(it)
            }
            inFlightStoryIds[storyType]?.takeIf { it.isActive } ?: backgroundScope.async {
                val ids = when (storyType) {
                    StoryType.TOP -> apiService.getTopStoryIds()
                    StoryType.NEW -> apiService.getNewStoryIds()
                }
                storyIdsCache[storyType] = ids
                ids
            }.also { inFlightStoryIds[storyType] = it }
        }

        return try {
            deferred.await()
        } finally {
            inFlightStoryIds.remove(storyType, deferred)
        }
    }

    /**
     * Fetches a single story by ID, deduplicating concurrent requests for the same ID
     * and catching exceptions so a single bad item never fails a batch.
     */
    private suspend fun fetchStoryDeduplicated(id: Long): StoryResponse? {
        storyDetailCache[id]?.let { return it }

        val deferred = storyFetchMutex.withLock {
            storyDetailCache[id]?.let {
                return@withLock kotlinx.coroutines.CompletableDeferred(it)
            }
            inFlightStories[id]?.takeIf { it.isActive } ?: backgroundScope.async {
                runCatching {
                    apiService.getStory(id)
                }.onSuccess { story ->
                    storyDetailCache[story.id] = story
                }.onFailure { e ->
                    println("HackerNewsPulseLog: Failed to fetch story $id: ${e.message}")
                }.getOrNull()
            }.also { inFlightStories[id] = it }
        }

        return try {
            deferred.await()
        } finally {
            inFlightStories.remove(id, deferred)
        }
    }

    override suspend fun preparePageOneSlots(storyType: StoryType, pageSize: Int): List<StoryResponse> {
        val storyIds = getOrFetchStoryIds(storyType)
        if (storyIds.isEmpty()) return emptyList()

        val windowSize = minOf(pageSize + PAGE_ONE_RESERVE_COUNT, storyIds.size)
        val primaryIds = storyIds.take(minOf(pageSize, storyIds.size))
        val reserveIds = if (storyIds.size > pageSize) {
            storyIds.subList(pageSize, windowSize)
        } else {
            emptyList()
        }

        // Initialize reserve queue and resolved map for Page 1
        pageOneReserveQueues[storyType] = ConcurrentLinkedQueue(reserveIds)
        pageOneResolvedSlots.putIfAbsent(storyType, ConcurrentHashMap())
        bulkIndexCursor.putIfAbsent(storyType, AtomicInteger(windowSize))

        // Kick off background bulk prefetch for Pages 2 and 3 right away
        prefetchNextPages(storyType, startPage = 2, pageSize = pageSize, pagesToPrefetch = 2)

        // Return already-resolved story if cached and valid, or shallow placeholder StoryResponse(id)
        return primaryIds.map { id ->
            val cached = pageOneResolvedSlots[storyType]?.get(id)
                ?: storyDetailCache[id]?.takeIf { !it.url.isNullOrBlank() }
            if (cached != null) {
                pageOneResolvedSlots[storyType]?.put(id, cached)
                cached
            } else {
                StoryResponse(id = id)
            }
        }
    }

    override suspend fun resolveStoryForCard(storyType: StoryType, id: Long): StoryResponse? {
        // 1. Check if this Page 1 slot was already resolved (including via reserve replacement)
        pageOneResolvedSlots[storyType]?.get(id)?.let { return it }

        // 2. Fetch the story for the requested ID
        val primaryStory = fetchStoryDeduplicated(id)
        if (primaryStory != null && !primaryStory.url.isNullOrBlank()) {
            pageOneResolvedSlots.getOrPut(storyType) { ConcurrentHashMap() }[id] = primaryStory
            return primaryStory
        }

        // 3. Primary story had no URL (e.g. Ask HN) or failed: pull a replacement from the reserve pool
        val reserveQueue = pageOneReserveQueues[storyType]
        while (reserveQueue != null) {
            val replacementId = reserveQueue.poll() ?: break
            val replacementStory = fetchStoryDeduplicated(replacementId)
            if (replacementStory != null && !replacementStory.url.isNullOrBlank()) {
                pageOneResolvedSlots.getOrPut(storyType) { ConcurrentHashMap() }[id] = replacementStory
                return replacementStory
            }
        }

        // 4. Fallback: pull from the main storyIds cursor if reserve queue was exhausted
        val storyIds = storyIdsCache[storyType] ?: emptyList()
        val cursor = bulkIndexCursor.getOrPut(storyType) { AtomicInteger(PAGE_ONE_WINDOW_SIZE) }
        while (true) {
            val nextIndex = cursor.getAndIncrement()
            if (nextIndex >= storyIds.size) break
            val candidateId = storyIds[nextIndex]
            val candidateStory = fetchStoryDeduplicated(candidateId)
            if (candidateStory != null && !candidateStory.url.isNullOrBlank()) {
                pageOneResolvedSlots.getOrPut(storyType) { ConcurrentHashMap() }[id] = candidateStory
                return candidateStory
            }
        }

        return null
    }

    override suspend fun getOrFetchBulkPage(
        storyType: StoryType,
        page: Int,
        pageSize: Int
    ): List<StoryResponse> {
        // Fast path: already cached
        getCachedStories(storyType, page, pageSize)?.takeIf { it.isNotEmpty() }?.let { cached ->
            // Trigger background prefetch for upcoming pages ahead of scroll
            prefetchNextPages(storyType, startPage = page + 1, pageSize = pageSize, pagesToPrefetch = 2)
            return cached
        }

        val loadedPage = getOrFetchBulkPageInternal(storyType, page, pageSize)

        // Proactively prefetch the next 2 pages in the background
        if (loadedPage.size >= pageSize) {
            prefetchNextPages(storyType, startPage = page + 1, pageSize = pageSize, pagesToPrefetch = 2)
        }

        return loadedPage
    }

    override fun prefetchNextPages(
        storyType: StoryType,
        startPage: Int,
        pageSize: Int,
        pagesToPrefetch: Int
    ) {
        backgroundScope.launch {
            for (pageOffset in 0 until pagesToPrefetch) {
                val targetPage = startPage + pageOffset
                val alreadyCached = cache[storyType]?.containsKey(targetPage) == true
                if (!alreadyCached) {
                    try {
                        println("HackerNewsPulseLog: Background bulk prefetching $storyType page $targetPage")
                        getOrFetchBulkPageInternal(storyType, targetPage, pageSize)
                    } catch (e: Exception) {
                        println("HackerNewsPulseLog: Background prefetch failed for $storyType page $targetPage: ${e.message}")
                    }
                }
            }
        }
    }

    private suspend fun getOrFetchBulkPageInternal(
        storyType: StoryType,
        page: Int,
        pageSize: Int
    ): List<StoryResponse> {
        cache[storyType]?.get(page)?.takeIf { it.isNotEmpty() }?.let { return it }

        val mutex = bulkPageMutexes.getOrPut(storyType) { Mutex() }
        return mutex.withLock {
            cache[storyType]?.get(page)?.takeIf { it.isNotEmpty() }?.let {
                recordCacheHit(storyType)
                return@withLock it.take(pageSize)
            }

            val storyIds = getOrFetchStoryIds(storyType)
            if (storyIds.isEmpty()) return@withLock emptyList()

            val cursor = bulkIndexCursor.getOrPut(storyType) {
                AtomicInteger(minOf(PAGE_ONE_WINDOW_SIZE, storyIds.size))
            }
            val overflowQueue = bulkOverflowQueues.getOrPut(storyType) { ConcurrentLinkedQueue() }

            val validStories = mutableListOf<StoryResponse>()
            // First drain any already-fetched valid stories from the overflow queue
            while (validStories.size < pageSize) {
                val overflowStory = overflowQueue.poll() ?: break
                validStories.add(overflowStory)
            }

            // Fetch remaining needed items in bulk batches
            while (validStories.size < pageSize) {
                val needed = pageSize - validStories.size
                // Over-fetch slightly (+3) to account for stories without URLs in a single round-trip
                val batchSize = needed + 3
                val startIndex = cursor.getAndAdd(batchSize)
                if (startIndex >= storyIds.size) break
                val endIndex = minOf(startIndex + batchSize, storyIds.size)
                val batchIds = storyIds.subList(startIndex, endIndex)

                val fetchedBatch = coroutineScope {
                    batchIds.map { id ->
                        async { fetchStoryDeduplicated(id) }
                    }.awaitAll()
                }.filterNotNull().filter { !it.url.isNullOrBlank() }

                for (story in fetchedBatch) {
                    if (validStories.size < pageSize) {
                        validStories.add(story)
                    } else {
                        overflowQueue.offer(story)
                    }
                }
            }

            if (validStories.isNotEmpty()) {
                addStoriesToCache(storyType, validStories, page)
                println("HackerNewsPulseLog: Bulk loaded & cached $storyType page $page (${validStories.size} items)")
            }
            validStories
        }
    }

    override suspend fun getCachedStories(storyType: StoryType, page: Int, pageSize: Int): List<StoryResponse>? {
        val typeCache = cache[storyType] ?: return null.also { recordCacheMiss(storyType) }

        val cachedPage = typeCache[page]
        return if (cachedPage != null && cachedPage.isNotEmpty()) {
            recordCacheHit(storyType)
            cachedPage.take(pageSize)
        } else {
            recordCacheMiss(storyType)
            null
        }
    }

    override suspend fun getStoryById(id: Long): StoryResponse? {
        return storyDetailCache[id]
    }

    override suspend fun saveStory(story: StoryResponse) {
        storyDetailCache[story.id] = story
    }

    override suspend fun addStoriesToCache(storyType: StoryType, stories: List<StoryResponse>, page: Int) {
        writeMutex.withLock {
            val typeCache = cache.getOrPut(storyType) { ConcurrentHashMap() }
            typeCache[page] = stories
            stories.forEach { story ->
                storyDetailCache[story.id] = story
            }
        }
    }

    override suspend fun invalidateCache(storyType: StoryType) {
        writeMutex.withLock {
            cache[storyType]?.clear()
            storyIdsCache.remove(storyType)
            pageOneResolvedSlots[storyType]?.clear()
            pageOneReserveQueues.remove(storyType)
            bulkIndexCursor.remove(storyType)
            bulkOverflowQueues.remove(storyType)
        }
    }

    override suspend fun hasCachedData(storyType: StoryType): Boolean {
        val typeCache = cache[storyType] ?: return false
        return typeCache.isNotEmpty()
    }

    override fun getCacheStats(): CacheStats {
        return stats
    }

    private suspend fun recordCacheHit(storyType: StoryType) {
        statsMutex.withLock {
            stats = when (storyType) {
                StoryType.TOP -> stats.copy(topStoriesHits = stats.topStoriesHits + 1)
                StoryType.NEW -> stats.copy(newStoriesHits = stats.newStoriesHits + 1)
            }
        }
    }

    private suspend fun recordCacheMiss(storyType: StoryType) {
        statsMutex.withLock {
            stats = when (storyType) {
                StoryType.TOP -> stats.copy(topStoriesMisses = stats.topStoriesMisses + 1)
                StoryType.NEW -> stats.copy(newStoriesMisses = stats.newStoriesMisses + 1)
            }
        }
    }
}