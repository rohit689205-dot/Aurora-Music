package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.Song
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)
    
    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: String): Song?
}
