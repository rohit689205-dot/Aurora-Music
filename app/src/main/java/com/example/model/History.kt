package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class History(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val songId: String,
    val playedAt: Long = System.currentTimeMillis(),
    val durationPlayed: Long = 0L
)
