package com.example.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MusicDatabase
import com.example.data.MusicRepository
import com.example.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.ui.state.UiState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.delay

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: MusicRepository
    val allSongsState: StateFlow<UiState<List<Song>>>

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    
    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    init {
        val songDao = MusicDatabase.getDatabase(application).songDao()
        repository = MusicRepository(songDao)
        allSongsState = repository.allSongs
            .map { list ->
                // Add a small artificial delay to show loading skeletons for demonstration
                delay(1000)
                if (list.isEmpty()) UiState.Empty else UiState.Success(list)
            }
            .catch { e -> emit(UiState.Error(e.message ?: "Unknown error")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )

        viewModelScope.launch {
            repository.initializeWithSampleDataIfEmpty()
        }
    }

    private var playbackJob: kotlinx.coroutines.Job? = null

    fun playSong(song: Song) {
        _currentSong.value = song
        _isPlaying.value = true
        _progress.value = 0f
        
        startPlaybackTimer()
    }

    private fun startPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (_isPlaying.value) {
                kotlinx.coroutines.delay(1000)
                if (_currentSong.value != null) {
                    val duration = _currentSong.value!!.duration.toFloat()
                    val currentProgress = _progress.value * duration
                    val newProgress = (currentProgress + 1000f) / duration
                    if (newProgress >= 1f) {
                        nextSong()
                    } else {
                        _progress.value = newProgress
                    }
                }
            }
        }
    }

    fun togglePlayPause() {
        if (_currentSong.value != null) {
            _isPlaying.value = !_isPlaying.value
            if (_isPlaying.value) {
                startPlaybackTimer()
            } else {
                playbackJob?.cancel()
            }
        }
    }
    
    fun nextSong() {
        val currentState = allSongsState.value
        if (currentState !is UiState.Success) return
        val currentList = currentState.data
        if (currentList.isEmpty()) return
        val currentIdx = currentList.indexOf(_currentSong.value)
        if (currentIdx != -1) {
            val nextIdx = (currentIdx + 1) % currentList.size
            playSong(currentList[nextIdx])
        } else {
            playSong(currentList.first())
        }
    }
    
    fun previousSong() {
        val currentState = allSongsState.value
        if (currentState !is UiState.Success) return
        val currentList = currentState.data
        if (currentList.isEmpty()) return
        val currentIdx = currentList.indexOf(_currentSong.value)
        if (currentIdx != -1) {
            val prevIdx = if (currentIdx - 1 < 0) currentList.size - 1 else currentIdx - 1
            playSong(currentList[prevIdx])
        } else {
            playSong(currentList.first())
        }
    }
}
