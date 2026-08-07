package com.example.playback

import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class AuroraMediaSessionService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    
    override fun onCreate() {
        super.onCreate()
        val player = AudioPlayerManager.getOrCreatePlayer(this)
        mediaSession = MediaSession.Builder(this, player).build()
    }
    
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }
    
    override fun onDestroy() {
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
