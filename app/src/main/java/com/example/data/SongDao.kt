package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs")
    fun getAllSongs(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<Song>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: Song)
    
    @Query("SELECT * FROM songs WHERE id = :id")
    suspend fun getSongById(id: String): Song?

    // Favorites
    @Query("SELECT * FROM favorites ORDER BY addedDate DESC")
    fun getAllFavorites(): Flow<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE songId = :songId")
    suspend fun removeFavorite(songId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE songId = :songId)")
    fun isFavorite(songId: String): Flow<Boolean>

    // Playlists
    @Query("SELECT * FROM playlists ORDER BY createdDate DESC")
    fun getAllPlaylists(): Flow<List<Playlist>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(playlistSong: PlaylistSong)

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId")
    fun getSongsForPlaylist(playlistId: String): Flow<List<PlaylistSong>>

    // Downloads
    @Query("SELECT * FROM downloads ORDER BY downloadDate DESC")
    fun getAllDownloads(): Flow<List<Download>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: Download)

    @Query("DELETE FROM downloads WHERE songId = :songId")
    suspend fun removeDownload(songId: String)

    @Query("DELETE FROM downloads")
    suspend fun clearAllDownloads()

    // History
    @Query("SELECT * FROM history ORDER BY playedAt DESC")
    fun getAllHistory(): Flow<List<History>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHistory(history: History)

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    // Search History
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC")
    fun getAllSearchHistory(): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSearchHistory(searchHistory: SearchHistory)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()
}

