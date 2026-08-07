package com.example.ui.player

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MusicDatabase
import com.example.data.MusicRepository
import com.example.model.Playlist
import com.example.model.Song
import com.example.playback.AudioPlayerManager
import com.example.ui.state.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PlayerViewModel(application: Application) : AndroidViewModel(application) {
    val repository: MusicRepository
    val allSongsState: StateFlow<UiState<List<Song>>>

    val currentSong: StateFlow<Song?> = AudioPlayerManager.currentSong
    val isPlaying: StateFlow<Boolean> = AudioPlayerManager.isPlaying
    val isBuffering: StateFlow<Boolean> = AudioPlayerManager.isBuffering
    val errorMessage: StateFlow<String?> = AudioPlayerManager.errorMessage

    val positionMs: StateFlow<Long> = AudioPlayerManager.positionMs
    val durationMs: StateFlow<Long> = AudioPlayerManager.durationMs

    val progress: StateFlow<Float> = combine(positionMs, durationMs) { pos, dur ->
        if (dur > 0) (pos.toFloat() / dur.toFloat()).coerceIn(0f, 1f) else 0f
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    private val _isShuffleEnabled = MutableStateFlow(false)
    val isShuffleEnabled = _isShuffleEnabled.asStateFlow()

    // 0: OFF, 1: REPEAT_ALL, 2: REPEAT_ONE
    private val _repeatMode = MutableStateFlow(0)
    val repeatMode = _repeatMode.asStateFlow()

    // Crossfade setting: 0, 3, or 5 seconds
    private val _crossfadeSeconds = MutableStateFlow(0)
    val crossfadeSeconds = _crossfadeSeconds.asStateFlow()

    private val _isCrossfading = MutableStateFlow(false)
    val isCrossfading = _isCrossfading.asStateFlow()

    private val _crossfadeVolume = MutableStateFlow(1.0f)
    val crossfadeVolume = _crossfadeVolume.asStateFlow()

    fun setCrossfadeDuration(seconds: Int) {
        _crossfadeSeconds.value = seconds
    }

    val userPlaylists: StateFlow<List<Playlist>>
    val isCurrentFavorite: StateFlow<Boolean>

    init {
        val songDao = MusicDatabase.getDatabase(application).songDao()
        repository = MusicRepository(songDao)

        // Initialize ExoPlayer
        AudioPlayerManager.getOrCreatePlayer(application)

        allSongsState = repository.allSongs
            .map { list ->
                if (list.isEmpty()) UiState.Empty else UiState.Success(list)
            }
            .catch { e -> emit(UiState.Error(e.message ?: "Unknown error")) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = UiState.Loading
            )

        userPlaylists = repository.allPlaylists
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        isCurrentFavorite = currentSong.flatMapLatest { song ->
            if (song != null) repository.isFavorite(song.id) else flowOf(false)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

        viewModelScope.launch {
            repository.initializeWithSampleDataIfEmpty()
        }

        // Monitor crossfade and track end
        viewModelScope.launch {
            combine(positionMs, durationMs, crossfadeSeconds) { pos, dur, xfadeSec ->
                Triple(pos, dur, xfadeSec)
            }.collect { (pos, dur, xfadeSec) ->
                if (dur > 0 && xfadeSec > 0) {
                    val remainingMs = dur - pos
                    val xfadeMs = xfadeSec * 1000L
                    if (remainingMs in 1..xfadeMs) {
                        _isCrossfading.value = true
                        val vol = (remainingMs.toFloat() / xfadeMs.toFloat()).coerceIn(0f, 1f)
                        _crossfadeVolume.value = vol
                        AudioPlayerManager.setVolume(vol)
                    } else {
                        _isCrossfading.value = false
                        _crossfadeVolume.value = 1.0f
                        AudioPlayerManager.setVolume(1.0f)
                    }
                } else {
                    _isCrossfading.value = false
                    _crossfadeVolume.value = 1.0f
                    AudioPlayerManager.setVolume(1.0f)
                }

                // Handle repeat one or track transition when end reached
                if (dur > 0 && pos >= dur - 300) {
                    if (repeatMode.value == 2) { // REPEAT_ONE
                        seekToProgress(0f)
                    }
                }
            }
        }
    }

    fun playSong(song: Song) {
        AudioPlayerManager.playSong(getApplication(), song)
        viewModelScope.launch {
            repository.addToHistory(song)
        }
    }

    fun togglePlayPause() {
        if (currentSong.value != null) {
            AudioPlayerManager.togglePlayPause()
        }
    }

    fun seekToProgress(fraction: Float) {
        val total = durationMs.value
        if (total > 0) {
            val target = (fraction * total).toLong()
            AudioPlayerManager.seekTo(target)
        }
    }

    fun toggleShuffle() {
        _isShuffleEnabled.value = !_isShuffleEnabled.value
    }

    fun toggleRepeat() {
        _repeatMode.value = (_repeatMode.value + 1) % 3
    }

    fun toggleFavoriteCurrentSong() {
        val song = currentSong.value ?: return
        viewModelScope.launch {
            repository.toggleFavorite(song)
        }
    }

    fun toggleFavoriteSong(song: Song) {
        viewModelScope.launch {
            repository.toggleFavorite(song)
        }
    }

    fun downloadCurrentSong(quality: String = "High", onComplete: () -> Unit = {}) {
        val song = currentSong.value ?: return
        viewModelScope.launch {
            repository.downloadSong(song, quality)
            onComplete()
        }
    }

    fun createPlaylist(title: String, description: String? = null, onCreated: (Playlist) -> Unit = {}) {
        viewModelScope.launch {
            val playlist = repository.createPlaylist(title, description)
            onCreated(playlist)
        }
    }

    fun addSongToPlaylist(playlistId: String, song: Song = currentSong.value!!, onAdded: () -> Unit = {}) {
        viewModelScope.launch {
            repository.addSongToPlaylist(playlistId, song)
            onAdded()
        }
    }

    fun nextSong() {
        val currentState = allSongsState.value
        if (currentState !is UiState.Success) return
        val currentList = currentState.data
        if (currentList.isEmpty()) return

        if (_isShuffleEnabled.value) {
            val randomSong = currentList.random()
            playSong(randomSong)
            return
        }

        val currentIdx = currentList.indexOf(currentSong.value)
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

        if (_isShuffleEnabled.value) {
            val randomSong = currentList.random()
            playSong(randomSong)
            return
        }

        val currentIdx = currentList.indexOf(currentSong.value)
        if (currentIdx != -1) {
            val prevIdx = if (currentIdx - 1 < 0) currentList.size - 1 else currentIdx - 1
            playSong(currentList[prevIdx])
        } else {
            playSong(currentList.first())
        }
    }
}
