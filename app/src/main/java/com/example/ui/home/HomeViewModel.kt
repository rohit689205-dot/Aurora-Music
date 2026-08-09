package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.providers.UnifiedMusicSearchRepository
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import com.example.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val unifiedRepository: UnifiedMusicSearchRepository = UnifiedMusicSearchRepository()
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
                // 1. Health check first
                try {
                    val healthResp = com.example.data.api.AuroraApiClient.apiService.healthCheck()
                    if (!healthResp.isSuccessful || healthResp.body()?.status != "ok") {
                        _homeState.value = UiState.Error("Aurora Music server is offline.")
                        return@launch
                    }
                } catch (e: Exception) {
                    _homeState.value = UiState.Error("Aurora Music server is offline.")
                    return@launch
                }

                val queryPrefix = mood ?: genre ?: "Arijit Singh"

                val trendingResult = unifiedRepository.searchAll(queryPrefix)
                val popularResult = unifiedRepository.searchAll("Popular in India")
                val hindiResult = unifiedRepository.searchAll("Hindi hit songs")
                val punjabiResult = unifiedRepository.searchAll("Punjabi hit songs")
                val bollywoodResult = unifiedRepository.searchAll("Bollywood film music")

                val trendingSongs = trendingResult.songs
                val popularSongs = popularResult.songs
                val hindiSongs = hindiResult.songs
                val punjabiSongs = punjabiResult.songs
                val bollywoodSongs = bollywoodResult.songs

                val allSongs = (trendingSongs + popularSongs + hindiSongs + punjabiSongs + bollywoodSongs).distinctBy { it.id }

                if (allSongs.isEmpty()) {
                    _homeState.value = UiState.Empty
                } else {
                    val artists = (trendingResult.artists + popularResult.artists + hindiResult.artists).distinctBy { it.name }.take(8)
                    val albums = (trendingResult.albums + popularResult.albums + hindiResult.albums).distinctBy { it.title }.take(8)

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
                    e.localizedMessage ?: "Unable to load music."
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

