package com.hackernewspulse.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.hackernewspulse.data.paging.StoryType
import com.hackernewspulse.data.remote.responses.StoryResponse
import com.hackernewspulse.data.repository.HackerNewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: HackerNewsRepository
) : ViewModel() {

    private val _stories = MutableStateFlow<PagingData<StoryResponse>>(PagingData.empty())
    val stories: StateFlow<PagingData<StoryResponse>> = _stories.asStateFlow()

    private val _storyType = MutableStateFlow(StoryType.TOP)
    val storyType: StateFlow<StoryType> = _storyType.asStateFlow()

    init {
        fetchStories()
    }

    fun setStoryType(type: StoryType) {
        _storyType.value = type
        fetchStories()
    }

    private fun fetchStories() {
        viewModelScope.launch {
            repository.getStories(_storyType.value).cachedIn(viewModelScope).collectLatest {
                _stories.value = it
            }
        }
    }
}
