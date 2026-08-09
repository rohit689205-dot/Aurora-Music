package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class Song(
    @PrimaryKey val id: String,
    val title: String,
    val artistId: String = "",
    val albumId: String = "",
    val duration: Long, // in ms
    val artworkUrl: String,
    val streamUrl: String = "",
    val localPath: String? = null,
    val genre: String? = null,
    val year: Int? = null,
    val discNumber: Int? = null,
    val trackNumber: Int? = null,
    val bitrate: Int? = null,
    val format: String? = null,
    val explicit: Boolean = false,
    val favorite: Boolean = false,
    val downloaded: Boolean = false,
    val playCount: Int = 0,
    val lastPlayed: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Kept for backward compatibility with existing UI
    val artist: String = "",
    val album: String = "",
    val isLocal: Boolean = false,

    // Playback control and provider metadata
    val playbackAvailable: Boolean = true,
    val sourceProvider: String = "YouTubeMusic",
    val isPreview: Boolean = false
)
