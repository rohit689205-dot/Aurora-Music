package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "albums")
data class Album(
    @PrimaryKey val id: String,
    val title: String,
    val artistId: String,
    val artwork: String,
    val releaseDate: Long,
    val genre: String? = null,
    val totalTracks: Int,
    val copyright: String? = null
)
