package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics")
data class Lyrics(
    @PrimaryKey val songId: String,
    val syncedLyrics: String? = null,
    val plainLyrics: String? = null,
    val language: String = "en",
    val provider: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)
