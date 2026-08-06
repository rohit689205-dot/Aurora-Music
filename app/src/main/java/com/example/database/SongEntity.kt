package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artistId: String,
    val albumId: String,
    val durationMs: Long,
    val artworkUri: String?,
    val mediaUri: String?,
    val genre: String?,
    val year: Int?,
    val isFavorite: Boolean,
    val isDownloaded: Boolean
)
