## Bug fixes

#### Fix "1 minutes ago"

The relative time since publication on card, formatted dynamically (e.g., `5 minutes ago`, `2 hours ago`, `3 days ago`) is showing plural for single items (e.g., `1 minutes ago`, `1 hours ago`, `1 days ago`). Please update it to show plural for plural items and singular for singular items
e.g., `1 minute ago`, `1 hour ago`, `1 day ago`, `5 minutes ago`, `2 hours ago`, `3 days ago`

✅ Done


#### No refresh screen while switching between categories
When clicking from 'top' to 'new' and viceversa, the existing list of stories should be cleaned up and "Loading Stories..." should be shown while new list is fetched. Currently it continues to show older rendered stories until new are recieved, leading to abad experience of user feeling that nothing happened on clicking the selector.

✅ Done

## Enhancements

### Faster loading of content

#### Try 1 - Gemini
Lets create a data layer which holds items so that UI layer reads from this data layer rather than making a network call. The data layer should hold list of top Stories and list of new stories. These should be pre-fetched on app load. When page renders, it should check in this data layer and render the items. If there are no items in data layer, it should fetch from API. This should be a blocking call for data render and it should show the existing loading indicator during this phase. When user does pull to refresh, the data layer items for that category should be purged and newly fetched. When user reaches end of scrolling list, API call should be made by data layer and items appended to existing list of items, which should then be rendered.

**Response**

This created an implementation with lot of refactoring and changes. The basic implementation was buggy and was not working correctly. Had to revert it back.

#### Try 2 - Kiro

**Prompt**

I want to enable caching for topStories and newStories on app load. When the user switches between top and new stories using the selector on top right corner fo the app, we currently always make network call. If we have the items loaded in cache on app start up, it will give a better user experience. The exact events for adding and removing from cache should be as follows
    - The cache for topStories and newStories should be loaded with items of first page of data, on app load. This should be as early in the app start up phase as possible
    - The cache should be invalidated and all items for the story category on screen removed only when user executes pull to refresh on the page. This should not remove items for the other story category which is not on screen.
    - When user scrolls down and loads more items on the page, they should also be updated in the cache, so that cache is up to date with the items shown on screen
    - When user selects different category from top/new selector, there should be no change to cache items. But the page render should check if there are items from cache and render those, thus not neededing a network call.

**Response**

Summary: 
I have successfully implemented all the core tasks for the story caching system in the HackerNewsPulse Android app. Here's what was accomplished:

✅ Completed Tasks:

* Core Cache Infrastructure - Created cache data models (CacheEntry, CacheStats) and StoryCacheManager interface with thread-safe implementation
* Enhanced Paging Source - Created CachedHackerNewsPagingSource that implements cache-first loading strategy
* Cache Preloading System - Implemented CachePreloader component integrated with app startup
* Repository Enhancement - Updated HackerNewsRepository to use cached paging source with invalidation support
* Dependency Injection - Added cache components to Hilt modules with proper singleton scopes
* UI Integration - Updated MainViewModel and MainScreen with cache invalidation for pull-to-refresh
* Cache Monitoring - Implemented cache statistics tracking and performance monitoring

🎯 Key Features Implemented:

* App Startup Preloading: Both TOP and NEW stories are preloaded on app launch
* Instant Category Switching: Cached data is served immediately when switching between story types
* Selective Cache Invalidation: Pull-to-refresh only clears cache for the active story type
* Pagination Sync: Cache is updated as users scroll and load more content
* Thread Safety: All cache operations use proper synchronization mechanisms
* Performance Monitoring: Cache hit/miss statistics are tracked for debugging

🔧 Architecture:

The implementation follows clean architecture principles with:
* Cache Layer: StoryCacheManager handles all caching logic
* Enhanced Paging: CachedHackerNewsPagingSource provides cache-first data loading
* Repository Pattern: Maintains existing interfaces while adding cache capabilities
* Dependency Injection: Proper Hilt integration with singleton scopes

The caching system is now fully functional and should provide the improved user experience you requested, with instant category switching and reduced network calls through intelligent caching.

