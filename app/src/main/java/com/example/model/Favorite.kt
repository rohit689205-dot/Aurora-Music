package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val songId: String,
    val addedDate: Long = System.currentTimeMillis()
)
