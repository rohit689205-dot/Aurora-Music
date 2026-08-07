package com.example.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.YouTubePlaylistData
import com.example.data.YouTubeRepository
import com.example.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class YouTubePlaylistViewModel(
    private val repository: YouTubeRepository = YouTubeRepository()
) : ViewModel() {

    private val _playlistState = MutableStateFlow<UiState<YouTubePlaylistData>>(UiState.Loading)
    val playlistState: StateFlow<UiState<YouTubePlaylistData>> = _playlistState.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private var currentPlaylistId: String? = null
    private var currentNextPageToken: String? = null

    fun loadPlaylist(playlistId: String) {
        currentPlaylistId = playlistId
        viewModelScope.launch {
            _playlistState.value = UiState.Loading
            repository.getPlaylistData(playlistId = playlistId).collect { result ->
                result.fold(
                    onSuccess = { data ->
                        currentNextPageToken = data.nextPageToken
                        if (data.items.isEmpty()) {
                            _playlistState.value = UiState.Empty
                        } else {
                            _playlistState.value = UiState.Success(data)
                        }
                    },
                    onFailure = { error ->
                        _playlistState.value = UiState.Error(error.localizedMessage ?: "Failed to load playlist from YouTube")
                    }
                )
            }
        }
    }

    fun loadNextPage() {
        val pId = currentPlaylistId ?: return
        val token = currentNextPageToken ?: return
        if (_isLoadingMore.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            repository.getPlaylistData(playlistId = pId, pageToken = token).collect { result ->
                result.fold(
                    onSuccess = { newData ->
                        currentNextPageToken = newData.nextPageToken
                        val currentData = (_playlistState.value as? UiState.Success)?.data
                        if (currentData != null) {
                            val combinedItems = currentData.items + newData.items
                            _playlistState.value = UiState.Success(
                                currentData.copy(items = combinedItems, nextPageToken = newData.nextPageToken)
                            )
                        }
                        _isLoadingMore.value = false
                    },
                    onFailure = {
                        _isLoadingMore.value = false
                    }
                )
            }
        }
    }

    fun retry() {
        currentPlaylistId?.let { loadPlaylist(it) }
    }
}
