package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.Song

@Database(
    entities = [
        Song::class,
        com.example.model.Album::class,
        com.example.model.Artist::class,
        com.example.model.Playlist::class,
        com.example.model.PlaylistSong::class,
        com.example.model.Download::class,
        com.example.model.History::class,
        com.example.model.SearchHistory::class,
        com.example.model.Favorite::class,
        com.example.model.Lyrics::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getDatabase(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "aurora_music_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
