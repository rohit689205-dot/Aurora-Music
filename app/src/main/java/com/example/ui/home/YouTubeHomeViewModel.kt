package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.YouTubeRepository
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
    val selectedGenre: String? = null,
    val nextPageToken: String? = null
)

class YouTubeHomeViewModel(
    private val repository: YouTubeRepository = YouTubeRepository()
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
            artist = "Arijit Singh, Pritam, Amit...",
            badge = "UNCUT",
            imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
            song = Song(
                id = "uncut_bollywood",
                title = "Kesariya (Uncut Version)",
                artist = "Arijit Singh, Pritam",
                album = "Brahmastra",
                duration = 268000L,
                artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            )
        ),
        HomeCardItem(
            id = "hit_2",
            title = "Kollywood Hits",
            artist = "Anirudh Ravichander, Sid Sriram...",
            badge = "HITLIST",
            imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
            song = Song(
                id = "kollywood_hits",
                title = "Hukum - Thalaivar Alappara",
                artist = "Anirudh Ravichander",
                album = "Jailer",
                duration = 207000L,
                artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
            )
        ),
        HomeCardItem(
            id = "hit_3",
            title = "Hits of 2026",
            artist = "Shashwat Sachdev, Rashmeet Kaur...",
            badge = "2026",
            imageUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
            song = Song(
                id = "hits_2026",
                title = "Aurora Nights",
                artist = "Shashwat Sachdev",
                album = "Aurora Hits",
                duration = 195000L,
                artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
            )
        ),
        HomeCardItem(
            id = "hit_4",
            title = "Punjabi Pop Wave",
            artist = "Diljit Dosanjh, Karan Aujla...",
            badge = "PUNJABI",
            imageUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop&q=80",
            song = Song(
                id = "punjabi_hits",
                title = "Lover (Aurora Mix)",
                artist = "Diljit Dosanjh",
                album = "MoonChild Era",
                duration = 182000L,
                artworkUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
            )
        )
    )

    private val defaultDancingHits = listOf(
        HomeCardItem(
            id = "dance_1",
            title = "Tauba Tauba",
            artist = "Karan Aujla, Badshah",
            badge = "DANCE",
            imageUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80",
            song = Song(
                id = "dance_tauba",
                title = "Tauba Tauba",
                artist = "Karan Aujla",
                album = "Bad Newz",
                duration = 205000L,
                artworkUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"
            )
        ),
        HomeCardItem(
            id = "dance_2",
            title = "Chaleya Party Mix",
            artist = "Arijit Singh, Shilpa Rao",
            badge = "PARTY",
            imageUrl = "https://images.unsplash.com/photo-1429962714451-bb934ecdc436?w=600&auto=format&fit=crop&q=80",
            song = Song(
                id = "dance_chaleya",
                title = "Chaleya (Club Remix)",
                artist = "Arijit Singh, Shilpa Rao",
                album = "Jawan",
                duration = 200000L,
                artworkUrl = "https://images.unsplash.com/photo-1429962714451-bb934ecdc436?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3"
            )
        ),
        HomeCardItem(
            id = "dance_3",
            title = "Naatu Naatu Beat",
            artist = "M.M. Keeravaani, Rahul Sipligunj",
            badge = "HIGH ENERGY",
            imageUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
            song = Song(
                id = "dance_naatu",
                title = "Naatu Naatu",
                artist = "Rahul Sipligunj, Kaala Bhairava",
                album = "RRR",
                duration = 215000L,
                artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3"
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
                mood != null -> "$mood music hits"
                genre != null -> "$genre songs music"
                category == "Popular" -> "India top hits music"
                else -> "$category music official video"
            }

            repository.search(query = query, musicCategoryOnly = true).collect { result ->
                result.fold(
                    onSuccess = { searchResult ->
                        val songsList = if (searchResult.songs.isNotEmpty()) searchResult.songs else getFallbackSongs(mood ?: genre ?: category)
                        _homeState.value = UiState.Success(
                            HomeMusicData(
                                popularMusic = songsList,
                                categorySongs = songsList,
                                biggestHitsCards = defaultBiggestHits,
                                dancingHitsCards = defaultDancingHits,
                                selectedCategory = category,
                                selectedMood = mood,
                                selectedGenre = genre,
                                nextPageToken = searchResult.nextPageToken
                            )
                        )
                    },
                    onFailure = {
                        val fallbackSongs = getFallbackSongs(mood ?: genre ?: category)
                        _homeState.value = UiState.Success(
                            HomeMusicData(
                                popularMusic = fallbackSongs,
                                categorySongs = fallbackSongs,
                                biggestHitsCards = defaultBiggestHits,
                                dancingHitsCards = defaultDancingHits,
                                selectedCategory = category,
                                selectedMood = mood,
                                selectedGenre = genre
                            )
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

    private fun getFallbackSongs(tag: String): List<Song> {
        return listOf(
            Song(
                id = "fb_1",
                title = "$tag Hits Special",
                artist = "Arijit Singh, Pritam",
                album = "Aurora Essentials",
                duration = 240000L,
                artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            ),
            Song(
                id = "fb_2",
                title = "$tag Vibez",
                artist = "Anirudh Ravichander",
                album = "Aurora Top Tracks",
                duration = 210000L,
                artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
            ),
            Song(
                id = "fb_3",
                title = "$tag Chill Anthem",
                artist = "Prateek Kuhad, Anuv Jain",
                album = "Indie Sessions",
                duration = 198000L,
                artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
            ),
            Song(
                id = "fb_4",
                title = "$tag Dynamic Beat",
                artist = "Badshah, Divine",
                album = "Street Beats",
                duration = 185000L,
                artworkUrl = "https://images.unsplash.com/photo-1516450360452-9312f5e86fc7?w=600&auto=format&fit=crop&q=80",
                streamUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
            )
        )
    }
}

