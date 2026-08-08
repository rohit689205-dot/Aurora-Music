package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.YTMusicRepository
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import com.example.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

class HomeViewModel(
    private val repository: YTMusicRepository = YTMusicRepository()
) : ViewModel() {

    private val _homeState = MutableStateFlow<UiState<HomeMusicData>>(UiState.Loading)
    val homeState: StateFlow<UiState<HomeMusicData>> = _homeState.asStateFlow()

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
            _homeState.value = UiState.Loading
            try {
                val queryPrefix = mood ?: genre ?: "Indian trending"

                val trendingResult = repository.search("Indian trending hits").first()
                val popularResult = repository.search("Popular in India").first()
                val hindiResult = repository.search(if (mood != null || genre != null) queryPrefix else "Hindi hit songs").first()
                val punjabiResult = repository.search("Punjabi hit songs").first()
                val bollywoodResult = repository.search("Bollywood film music").first()

                val trendingSongs = trendingResult.getOrNull()?.songs ?: emptyList()
                val popularSongs = popularResult.getOrNull()?.songs ?: emptyList()
                val hindiSongs = hindiResult.getOrNull()?.songs ?: emptyList()
                val punjabiSongs = punjabiResult.getOrNull()?.songs ?: emptyList()
                val bollywoodSongs = bollywoodResult.getOrNull()?.songs ?: emptyList()

                val allSongs = (trendingSongs + popularSongs + hindiSongs + punjabiSongs + bollywoodSongs).distinctBy { it.id }

                if (allSongs.isEmpty()) {
                    _homeState.value = UiState.Empty
                } else {
                    val artists = allSongs.map { it.artist }.distinct().take(8).map { name ->
                        Artist(
                            id = "artist_${name.hashCode()}",
                            name = name,
                            image = allSongs.firstOrNull { it.artist == name }?.artworkUrl ?: ""
                        )
                    }

                    val albums = allSongs.map { it.album }.distinct().take(8).map { title ->
                        val sample = allSongs.firstOrNull { it.album == title }
                        Album(
                            id = "album_${title.hashCode()}",
                            title = title,
                            artistId = "artist_${sample?.artist?.hashCode()}",
                            artwork = sample?.artworkUrl ?: "",
                            releaseDate = System.currentTimeMillis(),
                            totalTracks = 10
                        )
                    }

                    val playlists = listOf(
                        Playlist(id = "playlist_india_top", title = "India Top 50 Chart", description = "Trending hits across India"),
                        Playlist(id = "playlist_bollywood", title = "Bollywood Blockbusters", description = "Best of Hindi film soundtracks"),
                        Playlist(id = "playlist_punjabi", title = "Punjabi Club Anthems", description = "High energy Punjabi tracks")
                    )

                    _homeState.value = UiState.Success(
                        HomeMusicData(
                            indianTrending = trendingSongs.ifEmpty { allSongs.take(6) },
                            popularInIndia = popularSongs.ifEmpty { allSongs.drop(6).take(6) },
                            hindiHits = hindiSongs.ifEmpty { allSongs.take(8) },
                            punjabiHits = punjabiSongs.ifEmpty { allSongs.take(6) },
                            bollywoodMusic = bollywoodSongs.ifEmpty { allSongs.take(6) },
                            indianArtists = artists,
                            indianAlbums = albums,
                            recentlyPlayed = allSongs.shuffled().take(5),
                            userPlaylists = playlists,
                            selectedMood = mood,
                            selectedGenre = genre
                        )
                    )
                }
            } catch (e: Exception) {
                _homeState.value = UiState.Error(
                    e.localizedMessage ?: "Failed to connect to music service. Please check your connection."
                )
            }
        }
    }

    fun selectMood(mood: String) {
        val current = (_homeState.value as? UiState.Success)?.data
        val newMood = if (current?.selectedMood == mood) null else mood
        loadHomeData(mood = newMood, genre = null)
    }

    fun selectGenre(genre: String) {
        val current = (_homeState.value as? UiState.Success)?.data
        val newGenre = if (current?.selectedGenre == genre) null else genre
        loadHomeData(mood = null, genre = newGenre)
    }

    fun retry() {
        val current = (_homeState.value as? UiState.Success)?.data
        loadHomeData(mood = current?.selectedMood, genre = current?.selectedGenre)
    }
}

