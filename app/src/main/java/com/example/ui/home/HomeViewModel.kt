package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.YTMusicRepository
import com.example.model.Song
import com.example.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeCardItem(
    val id: String,
    val title: String,
    val artist: String,
    val badge: String,
    val imageUrl: String,
    val song: Song
)

data class HomeMusicData(
    val popularMusic: List<Song> = emptyList(),
    val categorySongs: List<Song> = emptyList(),
    val biggestHitsCards: List<HomeCardItem> = emptyList(),
    val dancingHitsCards: List<HomeCardItem> = emptyList(),
    val selectedCategory: String = "Popular",
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

    private val defaultBiggestHits = listOf(
        HomeCardItem(
            id = "hit_1",
            title = "Uncut Bollywood",
            artist = "Arijit Singh, Pritam",
            badge = "UNCUT",
            imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            song = Song(
                id = "uncut_bollywood",
                title = "Kesariya",
                artist = "Arijit Singh, Pritam",
                album = "Brahmastra",
                duration = 268000L,
                artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://preview.saavncdn.com/871/y0noToTML4XUhpycB5ml5IZ2Z3zWvvp9_96_p.mp4"
            )
        ),
        HomeCardItem(
            id = "hit_2",
            title = "Top Global Hits",
            artist = "The Weeknd, Dua Lipa",
            badge = "HITLIST",
            imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
            song = Song(
                id = "global_hits",
                title = "Blinding Lights",
                artist = "The Weeknd",
                album = "After Hours",
                duration = 200000L,
                artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
                streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI
            )
        )
    )

    private val defaultDancingHits = listOf(
        HomeCardItem(
            id = "dance_1",
            title = "Club Beats",
            artist = "Badshah, Karan Aujla",
            badge = "DANCE",
            imageUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80",
            song = Song(
                id = "dance_tauba",
                title = "Tauba Tauba",
                artist = "Karan Aujla",
                album = "Bad Newz",
                duration = 205000L,
                artworkUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80",
                streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI
            )
        )
    )

    init {
        loadHomeData("Popular")
    }

    fun loadHomeData(category: String, mood: String? = null, genre: String? = null) {
        viewModelScope.launch {
            _homeState.value = UiState.Loading
            val query = when {
                mood != null -> mood
                genre != null -> genre
                category == "Popular" -> "Arijit Singh"
                else -> category
            }

            repository.search(query = query).collect { result ->
                result.fold(
                    onSuccess = { searchResult ->
                        val songsList = searchResult.songs
                        if (songsList.isEmpty()) {
                            _homeState.value = UiState.Empty
                        } else {
                            _homeState.value = UiState.Success(
                                HomeMusicData(
                                    popularMusic = songsList,
                                    categorySongs = songsList,
                                    biggestHitsCards = defaultBiggestHits,
                                    dancingHitsCards = defaultDancingHits,
                                    selectedCategory = category,
                                    selectedMood = mood,
                                    selectedGenre = genre
                                )
                            )
                        }
                    },
                    onFailure = { error ->
                        _homeState.value = UiState.Error(
                            error.localizedMessage ?: "Failed to load music from YouTube Music."
                        )
                    }
                )
            }
        }
    }

    fun selectMood(mood: String) {
        val current = (_homeState.value as? UiState.Success)?.data
        val newMood = if (current?.selectedMood == mood) null else mood
        loadHomeData(category = current?.selectedCategory ?: "Popular", mood = newMood, genre = null)
    }

    fun selectGenre(genre: String) {
        val current = (_homeState.value as? UiState.Success)?.data
        val newGenre = if (current?.selectedGenre == genre) null else genre
        loadHomeData(category = current?.selectedCategory ?: "Popular", mood = null, genre = newGenre)
    }

    fun retry() {
        val current = (_homeState.value as? UiState.Success)?.data
        loadHomeData(
            category = current?.selectedCategory ?: "Popular",
            mood = current?.selectedMood,
            genre = current?.selectedGenre
        )
    }
}
