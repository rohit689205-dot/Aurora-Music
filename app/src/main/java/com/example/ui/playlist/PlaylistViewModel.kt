package com.example.ui.playlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MusicDatabase
import com.example.data.MusicRepository
import com.example.data.YTMusicPlaylistData
import com.example.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MusicRepository(MusicDatabase.getDatabase(application).songDao())

    private val _playlistState = MutableStateFlow<UiState<YTMusicPlaylistData>>(UiState.Loading)
    val playlistState: StateFlow<UiState<YTMusicPlaylistData>> = _playlistState.asStateFlow()

    private var currentPlaylistId: String? = null

    fun loadPlaylist(playlistId: String) {
        currentPlaylistId = playlistId
        viewModelScope.launch {
            _playlistState.value = UiState.Loading
            repository.getPlaylistData(getApplication(), playlistId).collect { result ->
                result.fold(
                    onSuccess = { data ->
                        _playlistState.value = UiState.Success(data)
                    },
                    onFailure = { error ->
                        _playlistState.value = UiState.Error(error.localizedMessage ?: "Failed to load playlist")
                    }
                )
            }
        }
    }

    fun retry() {
        currentPlaylistId?.let { loadPlaylist(it) }
    }
}
