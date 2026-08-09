package com.example.playback

import android.net.Uri
import com.example.model.Song

interface PlaybackProvider {
    fun resolvePlayableUri(song: Song): Uri?
    fun validatePlayableUrl(url: String): Boolean
}
