package com.example.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.model.Song

// Skeletal entity definitions and database configuration
@Database(entities = [SongEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
}
