package com.hackernewspulse.data.cache

import com.hackernewspulse.data.paging.CachedHackerNewsPagingSource
import com.hackernewspulse.data.paging.StoryType
import com.hackernewspulse.data.remote.HackerNewsApiService
import com.hackernewspulse.data.remote.responses.StoryResponse
import androidx.paging.PagingSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class StoryCacheManagerTest {

    private class FakeHackerNewsApiService(
        private val topIds: List<Long>,
        private val storiesById: Map<Long, StoryResponse>,
        private val failIds: Set<Long> = emptySet()
    ) : HackerNewsApiService {
        val topIdsCalls = AtomicInteger(0)
        val storyCalls = AtomicInteger(0)

        override suspend fun getTopStoryIds(): List<Long> {
            topIdsCalls.incrementAndGet()
            delay(10)
            return topIds
        }

        override suspend fun getNewStoryIds(): List<Long> = emptyList()

        override suspend fun getStory(id: Long): StoryResponse {
            storyCalls.incrementAndGet()
            delay(5)
            if (id in failIds) {
                throw RuntimeException("Simulated network error for story $id")
            }
            return storiesById[id] ?: StoryResponse(id = id, title = "Story $id", url = "https://example.com/$id")
        }
    }

    @Test
    fun `page 1 progressive slot replaces URL-less story from reserve pool without gaps`() = runBlocking {
        // ID 2 and ID 5 have no URL (e.g. Ask HN); ID 11 and ID 12 in reserve have valid URLs
        val ids = (1L..30L).toList()
        val stories = ids.associateWith { id ->
            val url = if (id == 2L || id == 5L) null else "https://example.com/$id"
            StoryResponse(id = id, title = "Title $id", url = url)
        }
        val fakeApi = FakeHackerNewsApiService(topIds = ids, storiesById = stories)
        val cacheManager = StoryCacheManagerImpl(fakeApi)

        // Prepare 10 slots for Page 1
        val slots = cacheManager.preparePageOneSlots(StoryType.TOP, pageSize = 10)
        assertEquals(10, slots.size)

        // Resolve all 10 slots (as StoryCard LaunchedEffect does)
        val resolvedStories = slots.mapNotNull { slot ->
            cacheManager.resolveStoryForCard(StoryType.TOP, slot.id)
        }

        // All 10 slots must resolve to non-null stories with valid non-blank URLs!
        assertEquals(10, resolvedStories.size)
        assertTrue(resolvedStories.all { !it.url.isNullOrBlank() })
        // IDs 2 and 5 should have been replaced by reserve IDs 11 and 12
        val resolvedIds = resolvedStories.map { it.id }.toSet()
        assertTrue(11L in resolvedIds)
        assertTrue(12L in resolvedIds)
    }

    @Test
    fun `pages 2 and 3 bulk fetch returns hydrated URL-filtered stories and caches them`() = runBlocking {
        val ids = (1L..60L).toList()
        // Make every 4th story URL-less and story 23 fail with network exception
        val stories = ids.associateWith { id ->
            val url = if (id % 4L == 0L) "" else "https://example.com/$id"
            StoryResponse(id = id, title = "Title $id", url = url)
        }
        val fakeApi = FakeHackerNewsApiService(
            topIds = ids,
            storiesById = stories,
            failIds = setOf(23L)
        )
        val cacheManager = StoryCacheManagerImpl(fakeApi)
        val pagingSource = CachedHackerNewsPagingSource(fakeApi, StoryType.TOP, cacheManager)

        // Load Page 1
        val page1Result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = 1, loadSize = 10, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page
        assertEquals(10, page1Result.data.size)
        assertEquals(2, page1Result.nextKey)

        // Load Page 2 (bulk hydrated)
        val page2Result = pagingSource.load(
            PagingSource.LoadParams.Append(key = 2, loadSize = 10, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page

        assertEquals(10, page2Result.data.size)
        // Every story in Page 2 must already be fully hydrated (non-null title and non-blank URL)
        assertTrue(page2Result.data.all { !it.title.isNullOrBlank() && !it.url.isNullOrBlank() })

        // Load Page 3 (should also be bulk hydrated with 10 valid items, skipping failed ID 23)
        val page3Result = pagingSource.load(
            PagingSource.LoadParams.Append(key = 3, loadSize = 10, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page

        assertEquals(10, page3Result.data.size)
        assertTrue(page3Result.data.all { !it.title.isNullOrBlank() && !it.url.isNullOrBlank() })
        assertNull(page3Result.data.find { it.id == 23L })

        // Ensure no duplicate stories between Page 2 and Page 3
        val page2Ids = page2Result.data.map { it.id }.toSet()
        val page3Ids = page3Result.data.map { it.id }.toSet()
        assertTrue((page2Ids intersect page3Ids).isEmpty())
    }
}
