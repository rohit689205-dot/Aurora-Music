package com.example.data

import com.example.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class MusicRepository(private val songDao: SongDao) {
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()
    val allFavorites: Flow<List<Favorite>> = songDao.getAllFavorites()
    val allPlaylists: Flow<List<Playlist>> = songDao.getAllPlaylists()
    val allDownloads: Flow<List<Download>> = songDao.getAllDownloads()
    val allHistory: Flow<List<History>> = songDao.getAllHistory()
    val allSearchHistory: Flow<List<SearchHistory>> = songDao.getAllSearchHistory()

    suspend fun getSongById(id: String): Song? = songDao.getSongById(id)

    suspend fun insertSong(song: Song) {
        songDao.insertSong(song)
    }

    fun isFavorite(songId: String): Flow<Boolean> = songDao.isFavorite(songId)

    suspend fun toggleFavorite(song: Song) {
        songDao.insertSong(song)
        val currentFavs = allFavorites.first()
        val exists = currentFavs.any { it.songId == song.id }
        if (exists) {
            songDao.removeFavorite(song.id)
        } else {
            songDao.addFavorite(Favorite(songId = song.id))
        }
    }

    suspend fun createPlaylist(title: String, description: String? = null): Playlist {
        val id = UUID.randomUUID().toString()
        val playlist = Playlist(id = id, title = title, description = description)
        songDao.insertPlaylist(playlist)
        return playlist
    }

    suspend fun addSongToPlaylist(playlistId: String, song: Song) {
        songDao.insertSong(song)
        val playlistSong = PlaylistSong(playlistId = playlistId, songId = song.id)
        songDao.insertPlaylistSong(playlistSong)
    }

    suspend fun downloadSong(song: Song, quality: String = "High"): Download {
        songDao.insertSong(song)
        val download = Download(
            id = UUID.randomUUID().toString(),
            songId = song.id,
            quality = quality,
            progress = 1.0f,
            status = "Completed",
            size = (3.5 * 1024 * 1024).toLong()
        )
        songDao.insertDownload(download)
        return download
    }

    suspend fun removeDownload(songId: String) {
        songDao.removeDownload(songId)
    }

    suspend fun clearAllDownloads() {
        songDao.clearAllDownloads()
    }

    suspend fun addToHistory(song: Song) {
        songDao.insertSong(song)
        songDao.addHistory(History(songId = song.id))
    }

    suspend fun clearHistory() {
        songDao.clearHistory()
    }

    suspend fun addSearchHistory(query: String) {
        songDao.addSearchHistory(SearchHistory(query = query))
    }

    suspend fun clearSearchHistory() {
        songDao.clearSearchHistory()
    }

    suspend fun initializeWithSampleDataIfEmpty() {
        val currentSongs = allSongs.first()
        if (currentSongs.isEmpty()) {
            val sampleSongs = listOf(
                Song(id = "1", title = "Midnight City (Authorized Preview)", artist = "M83", album = "Hurry Up, We're Dreaming", duration = 243000, artworkUrl = "https://upload.wikimedia.org/wikipedia/en/2/23/M83_-_Hurry_Up%2C_We%27re_Dreaming.png", streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI),
                Song(id = "2", title = "Blinding Lights (Authorized Preview)", artist = "The Weeknd", album = "After Hours", duration = 200000, artworkUrl = "https://upload.wikimedia.org/wikipedia/en/e/e6/The_Weeknd_-_Blinding_Lights.png", streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI),
                Song(id = "3", title = "Levitating (Authorized Preview)", artist = "Dua Lipa", album = "Future Nostalgia", duration = 203000, artworkUrl = "https://upload.wikimedia.org/wikipedia/en/f/f5/Dua_Lipa_-_Levitating.png", streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI),
                Song(id = "4", title = "Save Your Tears (Authorized Preview)", artist = "The Weeknd", album = "After Hours", duration = 215000, artworkUrl = "https://upload.wikimedia.org/wikipedia/en/e/e6/The_Weeknd_-_Blinding_Lights.png", streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI),
                Song(id = "5", title = "Good 4 U (Authorized Preview)", artist = "Olivia Rodrigo", album = "SOUR", duration = 178000, artworkUrl = "https://upload.wikimedia.org/wikipedia/en/b/b2/Olivia_Rodrigo_-_Good_4_U.png", streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI)
            )
            songDao.insertSongs(sampleSongs)
        }
    }
}

