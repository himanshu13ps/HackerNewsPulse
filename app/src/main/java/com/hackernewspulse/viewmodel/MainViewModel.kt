package com.hackernewspulse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.hackernewspulse.data.paging.StoryType
import com.hackernewspulse.data.pref.AppTheme
import com.hackernewspulse.data.pref.ThemeSettings
import com.hackernewspulse.data.remote.responses.StoryResponse
import com.hackernewspulse.data.repository.HackerNewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: HackerNewsRepository,
    private val themeSettings: ThemeSettings
) : ViewModel() {

    private val _stories = MutableStateFlow<PagingData<StoryResponse>>(PagingData.empty())
    val stories: StateFlow<PagingData<StoryResponse>> = _stories.asStateFlow()

    private val _storyType = MutableStateFlow(StoryType.TOP)
    val storyType: StateFlow<StoryType> = _storyType.asStateFlow()

    val theme: StateFlow<AppTheme> = themeSettings.themeStream
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )

    init {
        fetchStories()
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            themeSettings.setTheme(theme)
        }
    }

    suspend fun getStoryDetail(id: Long): StoryResponse {
        return repository.getStory(id)
    }

    fun setStoryType(type: StoryType) {
        _storyType.value = type
        fetchStories()
    }

    /**
     * Invalidates cache for the current story type and refreshes data
     * Called when user performs pull-to-refresh
     */
    fun refreshStories() {
        viewModelScope.launch {
            // Invalidate cache for current story type only
            repository.invalidateCache(_storyType.value)
            // Fetch fresh data
            fetchStories()
        }
    }

    /**
     * Gets cache statistics for debugging purposes
     * In a production app, this could be used for analytics or debugging
     */
    fun getCacheStats() = repository.getCacheStats()

    private fun fetchStories() {
        viewModelScope.launch {
            repository.getStories(_storyType.value)
                .cachedIn(viewModelScope)
                .collectLatest {
                    _stories.value = it
                }
        }
    }
}
