package com.example.ui.lyrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.LrcLibApiClient
import com.example.data.api.model.LrcLibResponse
import com.example.data.api.model.LyricLine
import com.example.model.Song
import com.example.playback.AudioPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LyricsUiState {
    object Idle : LyricsUiState
    object Loading : LyricsUiState
    data class Success(
        val song: Song,
        val lyricLines: List<LyricLine>,
        val plainLyrics: String?,
        val provider: String,
        val isSynced: Boolean
    ) : LyricsUiState
    data class Error(val message: String) : LyricsUiState
}

class LyricsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LyricsUiState>(LyricsUiState.Idle)
    val uiState: StateFlow<LyricsUiState> = _uiState.asStateFlow()

    private var currentSongId: String? = null

    init {
        viewModelScope.launch {
            AudioPlayerManager.currentSong.collect { song ->
                if (song != null && song.id != currentSongId) {
                    currentSongId = song.id
                    loadLyricsForSong(song)
                } else if (song == null) {
                    _uiState.value = LyricsUiState.Idle
                }
            }
        }
    }

    fun retryLoad() {
        val song = AudioPlayerManager.currentSong.value
        if (song != null) {
            loadLyricsForSong(song)
        }
    }

    private fun loadLyricsForSong(song: Song) {
        viewModelScope.launch {
            _uiState.value = LyricsUiState.Loading
            try {
                // Fetch from LRCLIB API (Echo Music lyrics source)
                val response: LrcLibResponse? = LrcLibApiClient.fetchLyrics(
                    trackName = song.title,
                    artistName = song.artist
                )

                if (response != null) {
                    val synced = response.syncedLyrics
                    val plain = response.plainLyrics

                    val lines = LrcLibApiClient.parseSyncedLyrics(synced)
                    val isSynced = lines.isNotEmpty()

                    _uiState.value = LyricsUiState.Success(
                        song = song,
                        lyricLines = lines,
                        plainLyrics = plain,
                        provider = "LRCLIB & Echo Music Open Source API",
                        isSynced = isSynced
                    )
                } else {
                    _uiState.value = LyricsUiState.Error("No lyrics found for \"${song.title}\" by ${song.artist}")
                }
            } catch (e: Exception) {
                _uiState.value = LyricsUiState.Error("Unable to fetch lyrics: ${e.localizedMessage ?: "Network error"}")
            }
        }
    }

    fun seekToTimestamp(timestampMs: Long) {
        AudioPlayerManager.seekTo(timestampMs)
    }
}
