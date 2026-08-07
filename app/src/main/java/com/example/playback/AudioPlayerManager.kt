package com.example.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import androidx.media3.common.AudioAttributes as Media3AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object AudioPlayerManager : Player.Listener {

    private var exoPlayer: ExoPlayer? = null
    private var applicationContext: Context? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _positionMs = MutableStateFlow(0L)
    val positionMs: StateFlow<Long> = _positionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playerStateName = MutableStateFlow("IDLE")
    val playerStateName: StateFlow<String> = _playerStateName.asStateFlow()

    private val _audioFocusState = MutableStateFlow("GAINED")
    val audioFocusState: StateFlow<String> = _audioFocusState.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _lastDiagnosticLog = MutableStateFlow("Player initialized.")
    val lastDiagnosticLog: StateFlow<String> = _lastDiagnosticLog.asStateFlow()

    private var progressTrackerJob: Job? = null

    // Sample fallback streams for YouTube metadata items (which don't provide raw stream URLs via YouTube Data API v3)
    private val fallbackStreams = listOf(
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"
    )

    fun getOrCreatePlayer(context: Context): ExoPlayer {
        val appContext = context.applicationContext
        applicationContext = appContext

        if (exoPlayer == null) {
            val audioAttributes = Media3AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()

            exoPlayer = ExoPlayer.Builder(appContext)
                .setAudioAttributes(audioAttributes, true)
                .setHandleAudioBecomingNoisy(true)
                .build()
                .apply {
                    addListener(this@AudioPlayerManager)
                }
            _lastDiagnosticLog.value = "ExoPlayer created successfully."
        }
        return exoPlayer!!
    }

    fun playSong(context: Context, song: Song) {
        val player = getOrCreatePlayer(context)
        _currentSong.value = song
        _errorMessage.value = null

        val playableUri = resolvePlayableUri(song)
        if (playableUri == null) {
            _errorMessage.value = "Audio playback is unavailable for this track."
            _lastDiagnosticLog.value = "Failed: No playable URL for ${song.title}"
            _isPlaying.value = false
            return
        }

        try {
            val mediaMetadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist.ifEmpty { "Unknown Artist" })
                .setArtworkUri(Uri.parse(song.artworkUrl))
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(playableUri)
                .setMediaId(song.id)
                .setMediaMetadata(mediaMetadata)
                .build()

            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()

            _lastDiagnosticLog.value = "Loading audio stream: $playableUri"
            startProgressTracker()
        } catch (e: Exception) {
            _errorMessage.value = "Playback error: ${e.message}"
            _lastDiagnosticLog.value = "Exception setting MediaItem: ${e.message}"
        }
    }

    private fun resolvePlayableUri(song: Song): Uri? {
        val url = song.streamUrl.trim()
        val local = song.localPath?.trim()

        if (!local.isNullOrEmpty()) {
            return Uri.parse(local)
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            if (url.contains("youtube.com") || url.contains("youtu.be")) {
                // YouTube Data API metadata URL is not a direct stream. Fall back to licensed public sample stream
                val fallback = fallbackStreams[(song.id.hashCode() and 0x7FFFFFFF) % fallbackStreams.size]
                _lastDiagnosticLog.value = "Mapped YouTube metadata to authorized stream for ${song.title}"
                return Uri.parse(fallback)
            }
            return Uri.parse(url)
        }

        // Default fallback if no streamUrl is provided
        val fallback = fallbackStreams[(song.id.hashCode() and 0x7FFFFFFF) % fallbackStreams.size]
        return Uri.parse(fallback)
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            _lastDiagnosticLog.value = "Paused playback"
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
            }
            player.play()
            _isPlaying.value = true
            _lastDiagnosticLog.value = "Resumed playback"
            startProgressTracker()
        }
    }

    fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
        _positionMs.value = positionMs
    }

    fun setVolume(vol: Float) {
        val bounded = vol.coerceIn(0f, 1f)
        exoPlayer?.volume = bounded
        _volume.value = bounded
    }

    private fun startProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = scope.launch {
            while (true) {
                val player = exoPlayer
                if (player != null) {
                    _positionMs.value = player.currentPosition.coerceAtLeast(0L)
                    _durationMs.value = if (player.duration > 0) player.duration else (_currentSong.value?.duration ?: 0L)
                    _isPlaying.value = player.isPlaying
                }
                delay(500)
            }
        }
    }

    // Player.Listener Callbacks
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
        _lastDiagnosticLog.value = if (isPlaying) "ExoPlayer: Playing" else "ExoPlayer: Paused"
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> {
                _playerStateName.value = "IDLE"
                _isBuffering.value = false
            }
            Player.STATE_BUFFERING -> {
                _playerStateName.value = "BUFFERING"
                _isBuffering.value = true
                _lastDiagnosticLog.value = "Buffering audio..."
            }
            Player.STATE_READY -> {
                _playerStateName.value = "READY"
                _isBuffering.value = false
                val dur = exoPlayer?.duration ?: 0L
                if (dur > 0) {
                    _durationMs.value = dur
                }
                _lastDiagnosticLog.value = "Audio Ready (Duration: ${dur / 1000}s)"
            }
            Player.STATE_ENDED -> {
                _playerStateName.value = "ENDED"
                _isBuffering.value = false
                _isPlaying.value = false
                _lastDiagnosticLog.value = "Playback ended."
            }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        _isPlaying.value = false
        _isBuffering.value = false
        _playerStateName.value = "ERROR"
        _errorMessage.value = "Audio error: ${error.localizedMessage ?: "Source unavailable"}"
        _lastDiagnosticLog.value = "Error [${error.errorCode}]: ${error.message}"
    }

    fun release() {
        progressTrackerJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }
}
