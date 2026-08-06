package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "artists")
data class Artist(
    @PrimaryKey val id: String,
    val name: String,
    val image: String,
    val biography: String? = null,
    val genres: String? = null, // Stored as comma-separated string for simplicity
    val followers: Int = 0,
    val verified: Boolean = false
)
