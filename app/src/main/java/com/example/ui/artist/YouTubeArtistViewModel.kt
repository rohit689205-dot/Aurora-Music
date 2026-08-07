package com.example.ui.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.YouTubeArtistData
import com.example.data.YouTubeRepository
import com.example.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class YouTubeArtistViewModel(
    private val repository: YouTubeRepository = YouTubeRepository()
) : ViewModel() {

    private val _artistState = MutableStateFlow<UiState<YouTubeArtistData>>(UiState.Loading)
    val artistState: StateFlow<UiState<YouTubeArtistData>> = _artistState.asStateFlow()

    private var currentArtistIdOrName: String? = null

    fun loadArtist(channelIdOrName: String) {
        currentArtistIdOrName = channelIdOrName
        viewModelScope.launch {
            _artistState.value = UiState.Loading
            repository.getArtistData(channelIdOrQuery = channelIdOrName).collect { result ->
                result.fold(
                    onSuccess = { data ->
                        _artistState.value = UiState.Success(data)
                    },
                    onFailure = { error ->
                        _artistState.value = UiState.Error(error.localizedMessage ?: "Failed to load channel details from YouTube")
                    }
                )
            }
        }
    }

    fun retry() {
        currentArtistIdOrName?.let { loadArtist(it) }
    }
}
