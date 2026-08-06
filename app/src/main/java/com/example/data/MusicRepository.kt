package com.example.data

import com.example.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class MusicRepository(private val songDao: SongDao) {
    val allSongs: Flow<List<Song>> = songDao.getAllSongs()

    suspend fun getSongById(id: String): Song? = songDao.getSongById(id)

    suspend fun initializeWithSampleDataIfEmpty() {
        val currentSongs = allSongs.first()
        if (currentSongs.isEmpty()) {
            val sampleSongs = listOf(
                Song(id = "1", title = "Midnight City", artist = "M83", album = "Hurry Up, We're Dreaming", duration = 243000, artworkUrl = "https://upload.wikimedia.org/wikipedia/en/2/23/M83_-_Hurry_Up%2C_We%27re_Dreaming.png"),
                Song(id = "2", title = "Blinding Lights", artist = "The Weeknd", album = "After Hours", duration = 200000, artworkUrl = "https://upload.wikimedia.org/wikipedia/en/e/e6/The_Weeknd_-_Blinding_Lights.png"),
                Song(id = "3", title = "Levitating", artist = "Dua Lipa", album = "Future Nostalgia", duration = 203000, artworkUrl = "https://upload.wikimedia.org/wikipedia/en/f/f5/Dua_Lipa_-_Levitating.png"),
                Song(id = "4", title = "Save Your Tears", artist = "The Weeknd", album = "After Hours", duration = 215000, artworkUrl = "https://upload.wikimedia.org/wikipedia/en/e/e6/The_Weeknd_-_Blinding_Lights.png"),
                Song(id = "5", title = "Good 4 U", artist = "Olivia Rodrigo", album = "SOUR", duration = 178000, artworkUrl = "https://upload.wikimedia.org/wikipedia/en/b/b2/Olivia_Rodrigo_-_Good_4_U.png")
            )
            songDao.insertSongs(sampleSongs)
        }
    }
}
