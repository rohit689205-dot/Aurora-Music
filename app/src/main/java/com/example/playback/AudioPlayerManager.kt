package com.example.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes as Media3AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
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

    // Known authorized direct audio stream for developer/diagnostic test
    const val OFFICIAL_TEST_AUDIO_URI = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3"

    fun getOrCreatePlayer(context: Context): ExoPlayer {
        val appContext = context.applicationContext
        applicationContext = appContext

        if (exoPlayer == null) {
            val audioAttributes = Media3AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()

            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("AuroraMusic/1.0 (Android ExoPlayer)")
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(15000)

            val dataSourceFactory = DefaultDataSource.Factory(appContext, httpDataSourceFactory)
            val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

            exoPlayer = ExoPlayer.Builder(appContext)
                .setMediaSourceFactory(mediaSourceFactory)
                .setAudioAttributes(audioAttributes, true) // Handles audio focus automatically
                .setHandleAudioBecomingNoisy(true)
                .build()
                .apply {
                    addListener(this@AudioPlayerManager)
                    volume = _volume.value
                }
            _lastDiagnosticLog.value = "ExoPlayer initialized with music AudioAttributes."
        }
        return exoPlayer!!
    }

    fun playSong(context: Context, song: Song) {
        val player = getOrCreatePlayer(context)
        _currentSong.value = song
        _errorMessage.value = null

        // Pre-playback validation
        if (song.id.isBlank() || song.title.isBlank() || song.artist.isBlank()) {
            player.stop()
            _isPlaying.value = false
            _isBuffering.value = false
            _playerStateName.value = "Unavailable"
            _errorMessage.value = "Audio unavailable."
            _lastDiagnosticLog.value = "Pre-playback validation failed: Incomplete track metadata."
            return
        }

        val playableUri = resolvePlayableUri(song)
        if (playableUri == null) {
            player.stop()
            _isPlaying.value = false
            _isBuffering.value = false
            _playerStateName.value = "Unavailable"
            _errorMessage.value = "Playback unavailable for this track."
            _lastDiagnosticLog.value = "Audio unavailable: Missing or invalid playable audio URL for track '${song.title}' (ID: ${song.id})."
            return
        }

        scope.launch {
            try {
                _isBuffering.value = true
                _playerStateName.value = "Loading"

                val mediaMetadata = MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist.ifEmpty { "Unknown Artist" })
                    .setArtworkUri(if (song.artworkUrl.isNotBlank()) Uri.parse(song.artworkUrl) else null)
                    .build()

                val mediaItem = MediaItem.Builder()
                    .setUri(playableUri)
                    .setMediaId(song.id)
                    .setMediaMetadata(mediaMetadata)
                    .build()

                _lastDiagnosticLog.value = "Loading track [ID: ${song.id}] '${song.title}' -> URI: $playableUri"

                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()

                startProgressTracker()
            } catch (e: Exception) {
                _isPlaying.value = false
                _isBuffering.value = false
                _playerStateName.value = "Error"
                _errorMessage.value = "Playback error: ${e.message}"
                _lastDiagnosticLog.value = "Exception initializing MediaItem: ${e.message}"
            }
        }
    }

    /**
     * Resolves playable URI. Returns null if URI is invalid, empty, or a non-direct web metadata URL (e.g. YouTube web links).
     */
    fun resolvePlayableUri(song: Song): Uri? {
        val local = song.localPath?.trim()
        if (!local.isNullOrEmpty()) {
            return Uri.parse(local)
        }

        val url = song.streamUrl.trim()
        if (url.startsWith("content://") || url.startsWith("file://")) {
            return Uri.parse(url)
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            val lower = url.lowercase()
            if (lower.contains("youtube.com/watch") || lower.contains("youtu.be/")) {
                // Non-direct watch URL, needs stream extraction
                return null
            }
            return Uri.parse(url)
        }

        return null
    }

    /**
     * Development / Diagnostic test player: loads and plays an authorized direct test track.
     */
    fun playTestTrack(context: Context, customUri: String = OFFICIAL_TEST_AUDIO_URI) {
        val testSong = Song(
            id = "test_track_001",
            title = "Official Audio Test Track",
            artist = "Google ExoPlayer Test Media",
            album = "Authorized Diagnostic Suite",
            duration = 60000L,
            artworkUrl = "",
            streamUrl = customUri
        )
        playSong(context, testSong)
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _lastDiagnosticLog.value = "User paused playback."
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0)
            }
            player.play()
            _lastDiagnosticLog.value = "User resumed playback."
            startProgressTracker()
        }
    }

    fun stop() {
        exoPlayer?.stop()
        _isPlaying.value = false
        _isBuffering.value = false
        _playerStateName.value = "IDLE"
        _lastDiagnosticLog.value = "Playback stopped."
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

    // Player.Listener Callbacks - Real ExoPlayer state is single source of truth
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
        if (isPlaying) {
            _playerStateName.value = "Playing"
        } else if (exoPlayer?.playbackState == Player.STATE_READY) {
            _playerStateName.value = "Paused"
        }
        _lastDiagnosticLog.value = if (isPlaying) "ExoPlayer Event: Playing (Audible)" else "ExoPlayer Event: Paused / Idle"
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> {
                _playerStateName.value = "IDLE"
                _isBuffering.value = false
            }
            Player.STATE_BUFFERING -> {
                _playerStateName.value = "Loading"
                _isBuffering.value = true
                _lastDiagnosticLog.value = "ExoPlayer: Buffering audio stream..."
            }
            Player.STATE_READY -> {
                _isBuffering.value = false
                _playerStateName.value = if (exoPlayer?.isPlaying == true) "Playing" else "Paused"
                val dur = exoPlayer?.duration ?: 0L
                if (dur > 0) {
                    _durationMs.value = dur
                }
                _lastDiagnosticLog.value = "ExoPlayer: Ready. Duration: ${dur / 1000}s"
            }
            Player.STATE_ENDED -> {
                _playerStateName.value = "Completed"
                _isBuffering.value = false
                _isPlaying.value = false
                _lastDiagnosticLog.value = "ExoPlayer: Track ended."
            }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        _isPlaying.value = false
        _isBuffering.value = false
        _playerStateName.value = "Error"
        
        val curr = _currentSong.value
        val msg = error.message ?: ""
        val code = error.errorCode

        val specificMessage = when {
            msg.contains("451") || code == 451 -> "This track isn't available for playback in your region."
            msg.contains("403") || code == 403 -> "Playback is unavailable for this track."
            msg.contains("404") -> "HTTP error 404: Audio stream not found."
            code == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED || msg.contains("Unable to resolve host") || msg.contains("No address associated") -> "You're offline."
            code == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "Network connection timeout."
            code == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "Audio file source not found."
            code == PlaybackException.ERROR_CODE_IO_NO_PERMISSION -> "Permission denied accessing audio source."
            code == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED -> "Audio decoder initialization failed."
            else -> if (msg.isNotBlank() && !msg.contains("Source error")) msg else "Unable to play this track."
        }

        _errorMessage.value = specificMessage
        _lastDiagnosticLog.value = "ERROR for Track '${curr?.title}' (ID: ${curr?.id}): $specificMessage"
    }

    fun release() {
        progressTrackerJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }
}
