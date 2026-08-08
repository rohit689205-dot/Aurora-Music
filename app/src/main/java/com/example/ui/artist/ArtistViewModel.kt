package com.example.ui.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.YTMusicArtistData
import com.example.data.YTMusicRepository
import com.example.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ArtistViewModel(
    private val repository: YTMusicRepository = YTMusicRepository()
) : ViewModel() {

    private val _artistState = MutableStateFlow<UiState<YTMusicArtistData>>(UiState.Loading)
    val artistState: StateFlow<UiState<YTMusicArtistData>> = _artistState.asStateFlow()

    private var currentArtistId: String? = null

    fun loadArtist(artistId: String) {
        currentArtistId = artistId
        viewModelScope.launch {
            _artistState.value = UiState.Loading
            repository.getArtistData(artistId = artistId).collect { result ->
                result.fold(
                    onSuccess = { data ->
                        _artistState.value = UiState.Success(data)
                    },
                    onFailure = { error ->
                        _artistState.value = UiState.Error(error.localizedMessage ?: "Failed to load artist details")
                    }
                )
            }
        }
    }

    fun retry() {
        currentArtistId?.let { loadArtist(it) }
    }
}
