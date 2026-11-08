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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: HackerNewsRepository,
    private val themeSettings: ThemeSettings
) : ViewModel() {

    private val _storyType = MutableStateFlow(StoryType.TOP)
    val storyType: StateFlow<StoryType> = _storyType.asStateFlow()

    private val refreshTrigger = MutableStateFlow(0)

    val stories: Flow<PagingData<StoryResponse>> = combine(_storyType, refreshTrigger) { type, _ ->
        type
    }.flatMapLatest { type ->
        repository.getStories(type)
    }.cachedIn(viewModelScope)

    val theme: StateFlow<AppTheme> = themeSettings.themeStream
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppTheme.SYSTEM
        )

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            themeSettings.setTheme(theme)
        }
    }

    suspend fun getStoryDetail(id: Long): StoryResponse? {
        return repository.resolveStoryForCard(_storyType.value, id)
    }

    fun setStoryType(type: StoryType) {
        if (_storyType.value != type) {
            _storyType.value = type
        }
    }

    /**
     * Invalidates cache for the current story type and refreshes data
     * Called when user performs pull-to-refresh
     */
    fun refreshStories() {
        viewModelScope.launch {
            // Invalidate cache for current story type only
            repository.invalidateCache(_storyType.value)
            // Trigger fresh Pager flow
            refreshTrigger.value += 1
        }
    }

    /**
     * Gets cache statistics for debugging purposes
     * In a production app, this could be used for analytics or debugging
     */
    fun getCacheStats() = repository.getCacheStats()
}
