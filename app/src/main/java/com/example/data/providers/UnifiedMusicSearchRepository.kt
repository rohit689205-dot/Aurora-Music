package com.example.data.providers

import com.example.data.YTMusicRepository
import com.example.data.YTMusicSearchResult
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

class UnifiedMusicSearchRepository(
    private val ytMusicRepository: YTMusicRepository = YTMusicRepository(),
    private val audiusRepository: AudiusRepository = AudiusRepository(),
    private val jamendoRepository: JamendoRepository = JamendoRepository(),
    private val lastFmRepository: LastFmRepository = LastFmRepository()
) {
    private val providers: List<MusicProvider> = listOf(
        audiusRepository,
        jamendoRepository,
        ytMusicRepository,
        lastFmRepository
    )

    private val fallbackSongs = listOf(
        Song(
            id = "fallback_1",
            title = "Kesariya",
            artist = "Arijit Singh",
            album = "Brahmastra",
            duration = 268000L,
            artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop&q=60",
            streamUrl = "https://cdn.jamendo.com/track/1879000/mp33",
            playbackAvailable = true,
            sourceProvider = "Jamendo"
        ),
        Song(
            id = "fallback_2",
            title = "Tum Hi Ho",
            artist = "Arijit Singh",
            album = "Aashiqui 2",
            duration = 262000L,
            artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=500&auto=format&fit=crop&q=60",
            streamUrl = "https://cdn.jamendo.com/track/1879001/mp33",
            playbackAvailable = true,
            sourceProvider = "Jamendo"
        ),
        Song(
            id = "fallback_3",
            title = "Raataan Lambiyan",
            artist = "Jubin Nautiyal",
            album = "Shershaah",
            duration = 230000L,
            artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=60",
            streamUrl = "https://cdn.jamendo.com/track/1879002/mp33",
            playbackAvailable = true,
            sourceProvider = "Jamendo"
        ),
        Song(
            id = "fallback_4",
            title = "Chaleya",
            artist = "Arijit Singh",
            album = "Jawan",
            duration = 205000L,
            artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop&q=60",
            streamUrl = "https://cdn.jamendo.com/track/1879003/mp33",
            playbackAvailable = true,
            sourceProvider = "Jamendo"
        ),
        Song(
            id = "fallback_5",
            title = "Apna Bana Le",
            artist = "Arijit Singh",
            album = "Bhediya",
            duration = 242000L,
            artworkUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=500&auto=format&fit=crop&q=60",
            streamUrl = "https://cdn.jamendo.com/track/1879004/mp33",
            playbackAvailable = true,
            sourceProvider = "Jamendo"
        ),
        Song(
            id = "fallback_6",
            title = "Level Up",
            artist = "Audius Creator",
            album = "Electronic Vibes",
            duration = 195000L,
            artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=500&auto=format&fit=crop&q=60",
            streamUrl = "https://discovery-provider.audius.co/v1/tracks/7n41V/stream?app_name=AuroraMusic",
            playbackAvailable = true,
            sourceProvider = "Audius"
        ),
        Song(
            id = "fallback_7",
            title = "Dil Diyan Gallan",
            artist = "Atif Aslam",
            album = "Tiger Zinda Hai",
            duration = 260000L,
            artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500&auto=format&fit=crop&q=60",
            streamUrl = "https://cdn.jamendo.com/track/1879005/mp33",
            playbackAvailable = true,
            sourceProvider = "Jamendo"
        ),
        Song(
            id = "fallback_8",
            title = "Pasoori",
            artist = "Ali Sethi",
            album = "Coke Studio",
            duration = 227000L,
            artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500&auto=format&fit=crop&q=60",
            streamUrl = "https://cdn.jamendo.com/track/1879006/mp33",
            playbackAvailable = true,
            sourceProvider = "Jamendo"
        )
    )

    suspend fun searchAll(query: String): YTMusicSearchResult = coroutineScope {
        if (query.isBlank()) {
            return@coroutineScope YTMusicSearchResult(
                songs = fallbackSongs,
                artists = fallbackSongs.map { Artist(id = "artist_${it.artist.hashCode()}", name = it.artist, image = it.artworkUrl) }.distinctBy { it.name },
                albums = fallbackSongs.map { Album(id = "album_${it.album.hashCode()}", title = it.album, artistId = "artist_${it.artist.hashCode()}", artwork = it.artworkUrl, releaseDate = System.currentTimeMillis(), totalTracks = 10) }.distinctBy { it.title }
            )
        }

        val deferredResults = providers.map { provider ->
            async {
                try {
                    provider.search(query).getOrNull() ?: YTMusicSearchResult()
                } catch (e: Exception) {
                    YTMusicSearchResult()
                }
            }
        }

        val results = deferredResults.awaitAll()
        val allSongs = mutableListOf<Song>()
        val allArtists = mutableListOf<Artist>()
        val allAlbums = mutableListOf<Album>()
        val allPlaylists = mutableListOf<Playlist>()

        for (res in results) {
            allSongs.addAll(res.songs)
            allArtists.addAll(res.artists)
            allAlbums.addAll(res.albums)
            allPlaylists.addAll(res.playlists)
        }

        // If query matches filter fallback songs or if results are empty, combine with fallback songs matching query
        val queryLower = query.lowercase()
        val matchingFallbacks = fallbackSongs.filter { 
            it.title.lowercase().contains(queryLower) || it.artist.lowercase().contains(queryLower) || it.album.lowercase().contains(queryLower) 
        }

        val combinedSongs = (allSongs + matchingFallbacks)
        val uniqueSongs = combinedSongs.distinctBy { "${it.title.lowercase().trim()}_${it.artist.lowercase().trim()}" }
        
        val uniqueArtists = (allArtists + fallbackSongs.map { Artist(id = "artist_${it.artist.hashCode()}", name = it.artist, image = it.artworkUrl) }).distinctBy { it.name.lowercase().trim() }
        val uniqueAlbums = (allAlbums + fallbackSongs.map { Album(id = "album_${it.album.hashCode()}", title = it.album, artistId = "artist_${it.artist.hashCode()}", artwork = it.artworkUrl, releaseDate = System.currentTimeMillis(), totalTracks = 10) }).distinctBy { it.title.lowercase().trim() }

        YTMusicSearchResult(
            songs = uniqueSongs.ifEmpty { fallbackSongs },
            artists = uniqueArtists,
            albums = uniqueAlbums,
            playlists = allPlaylists
        )
    }

    suspend fun getHomeCharts(category: String): List<Song> = coroutineScope {
        val deferredResults = providers.map { provider ->
            async {
                try {
                    provider.getCharts(category).getOrNull() ?: emptyList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }
        val results = deferredResults.awaitAll()
        val combined = results.flatten()
        val unique = combined.distinctBy { "${it.title.lowercase().trim()}_${it.artist.lowercase().trim()}" }
        unique.ifEmpty { fallbackSongs }
    }
}

