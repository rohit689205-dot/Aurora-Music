package com.example.model

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId"],
    indices = [Index("songId")]
)
data class PlaylistSong(
    val playlistId: String,
    val songId: String,
    val position: Int
)
