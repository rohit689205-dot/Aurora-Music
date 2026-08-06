package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey val id: String,
    val title: String,
    val description: String? = null,
    val artwork: String? = null,
    val owner: String = "User",
    val createdDate: Long = System.currentTimeMillis(),
    val modifiedDate: Long = System.currentTimeMillis(),
    val pinned: Boolean = false,
    val downloaded: Boolean = false
)
