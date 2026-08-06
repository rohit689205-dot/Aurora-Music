package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class Download(
    @PrimaryKey val id: String,
    val songId: String,
    val quality: String,
    val progress: Float = 0f,
    val status: String, // "Preparing", "Downloading", "Paused", "Completed", "Failed"
    val filePath: String? = null,
    val size: Long = 0L,
    val downloadDate: Long = System.currentTimeMillis()
)
