package com.example.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MusicDatabase
import com.example.data.MusicRepository
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val data: HomeMusicData) : HomeUiState
    object Empty : HomeUiState
    data class Error(val message: String) : HomeUiState
}

data class HomeMusicData(
    val indianTrending: List<Song> = emptyList(),
    val popularInIndia: List<Song> = emptyList(),
    val hindiHits: List<Song> = emptyList(),
    val punjabiHits: List<Song> = emptyList(),
    val bollywoodMusic: List<Song> = emptyList(),
    val indianArtists: List<Artist> = emptyList(),
    val indianAlbums: List<Album> = emptyList(),
    val recentlyPlayed: List<Song> = emptyList(),
    val userPlaylists: List<Playlist> = emptyList(),
    val isOffline: Boolean = false,
    val selectedMood: String? = null,
    val selectedGenre: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MusicRepository(MusicDatabase.getDatabase(application).songDao())

    private val _homeState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeState: StateFlow<HomeUiState> = _homeState.asStateFlow()

    val moodChips = listOf(
        "Romance", "Relax", "Feel Good", "Energise",
        "Focus", "Sleep", "Workout", "Party"
    )

    val genres = listOf(
        "Ghazal/Sufi", "Hindustani Classical", "Indie", "Gujarati",
        "Hip-Hop", "J-Pop", "Haryanvi", "Indian Indie",
        "Jazz", "Hindi", "Indian Pop", "K-Pop"
    )

    init {
        loadHomeData()
    }

    fun loadHomeData(mood: String? = null, genre: String? = null) {
        viewModelScope.launch {
            _homeState.value = HomeUiState.Loading
            try {
                repository.initializeWithSampleDataIfEmpty()

                val topIndianSongs = repository.getTopIndianChartSongs()
                val query = mood ?: genre ?: "Indian music"
                val searchResult = repository.search(getApplication(), query)
                
                // Combine curated top Indian hits ("9:45", "Blue Eyes", etc.) with search results
                val allSongs = (topIndianSongs + searchResult.songs).distinctBy { it.title.lowercase().trim() }

                if (allSongs.isEmpty()) {
                    _homeState.value = HomeUiState.Empty
                } else {
                    _homeState.value = HomeUiState.Success(
                        HomeMusicData(
                            indianTrending = allSongs.take(10),
                            popularInIndia = if (allSongs.size > 1) allSongs.drop(1).take(10) else allSongs,
                            hindiHits = allSongs.filter { it.artist.contains("Arijit") || it.artist.contains("King") || it.title.contains("Blue") || it.title.contains("Kahani") }.ifEmpty { allSongs.take(8) },
                            punjabiHits = allSongs.filter { it.title.contains("9:45") || it.artist.contains("Prabh") || it.artist.contains("Karan") || it.artist.contains("Honey") }.ifEmpty { allSongs.take(6) },
                            bollywoodMusic = allSongs.take(6),
                            indianArtists = searchResult.artists,
                            indianAlbums = searchResult.albums,
                            recentlyPlayed = allSongs.take(5),
                            userPlaylists = searchResult.playlists,
                            selectedMood = mood,
                            selectedGenre = genre
                        )
                    )
                }
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: "Unable to connect to music service."
                _homeState.value = HomeUiState.Error(msg)
            }
        }
    }

    fun selectMood(mood: String) {
        val current = (_homeState.value as? HomeUiState.Success)?.data
        val newMood = if (current?.selectedMood == mood) null else mood
        loadHomeData(mood = newMood, genre = null)
    }

    fun selectGenre(genre: String) {
        val current = (_homeState.value as? HomeUiState.Success)?.data
        val newGenre = if (current?.selectedGenre == genre) null else genre
        loadHomeData(mood = null, genre = newGenre)
    }

    fun retry() {
        val current = (_homeState.value as? HomeUiState.Success)?.data
        loadHomeData(mood = current?.selectedMood, genre = current?.selectedGenre)
    }
}
