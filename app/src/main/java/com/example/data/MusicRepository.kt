package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.api.JamendoApiClient
import com.example.data.api.PublicMusicApiClient
import com.example.data.api.SpotifyDiagnostics
import com.example.data.api.model.ITunesTrackDto
import com.example.data.api.model.JamendoTrackDto
import com.example.data.api.model.SpotifyAlbumDto
import com.example.data.api.model.SpotifyArtistDto
import com.example.data.api.model.SpotifyPlaylistDto
import com.example.data.api.model.SpotifyTrackDto
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
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
            val sampleSongs = getTopIndianChartSongs()
            songDao.insertSongs(sampleSongs)
        }
    }

    fun getTopIndianChartSongs(): List<Song> {
        return listOf(
            Song(
                id = "ind_945",
                title = "9:45",
                artist = "Prabh Singh & Jay Trak",
                album = "9:45 - Single",
                duration = 212000,
                artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop",
                streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI,
                sourceProvider = "IndianTopCharts",
                playbackAvailable = true
            ),
            Song(
                id = "ind_blue_eyes",
                title = "Blue Eyes",
                artist = "Yo Yo Honey Singh",
                album = "Blue Eyes - Single",
                duration = 200000,
                artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop",
                streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI,
                sourceProvider = "IndianTopCharts",
                playbackAvailable = true
            ),
            Song(
                id = "ind_maan_meri_jaan",
                title = "Maan Meri Jaan",
                artist = "King",
                album = "Champagne Talk",
                duration = 194000,
                artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop",
                streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI,
                sourceProvider = "IndianTopCharts",
                playbackAvailable = true
            ),
            Song(
                id = "ind_kahani_suno",
                title = "Kahani Suno 2.0",
                artist = "Kaifi Khalil",
                album = "Kahani Suno 2.0",
                duration = 173000,
                artworkUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop",
                streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI,
                sourceProvider = "IndianTopCharts",
                playbackAvailable = true
            ),
            Song(
                id = "ind_chaleya",
                title = "Chaleya",
                artist = "Arijit Singh & Shilpa Rao",
                album = "Jawan",
                duration = 200000,
                artworkUrl = "https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=600&auto=format&fit=crop",
                streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI,
                sourceProvider = "IndianTopCharts",
                playbackAvailable = true
            ),
            Song(
                id = "ind_softly",
                title = "Softly",
                artist = "Karan Aujla & Ikky",
                album = "Making Memories",
                duration = 154000,
                artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop",
                streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI,
                sourceProvider = "IndianTopCharts",
                playbackAvailable = true
            ),
            Song(
                id = "ind_kesariya",
                title = "Kesariya",
                artist = "Arijit Singh & Pritam",
                album = "Brahmastra",
                duration = 268000,
                artworkUrl = "https://images.unsplash.com/photo-1459749411175-04bf5292ceea?w=600&auto=format&fit=crop",
                streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI,
                sourceProvider = "IndianTopCharts",
                playbackAvailable = true
            ),
            Song(
                id = "ind_pasoori",
                title = "Pasoori",
                artist = "Ali Sethi & Shae Gill",
                album = "Coke Studio Season 14",
                duration = 224000,
                artworkUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop",
                streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI,
                sourceProvider = "IndianTopCharts",
                playbackAvailable = true
            ),
            Song(
                id = "ind_heeriye",
                title = "Heeriye",
                artist = "Jasleen Royal & Arijit Singh",
                album = "Heeriye - Single",
                duration = 194000,
                artworkUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop",
                streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI,
                sourceProvider = "IndianTopCharts",
                playbackAvailable = true
            ),
            Song(
                id = "ind_tum_hi_ho",
                title = "Tum Hi Ho",
                artist = "Arijit Singh & Mithoon",
                album = "Aashiqui 2",
                duration = 262000,
                artworkUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop",
                streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI,
                sourceProvider = "IndianTopCharts",
                playbackAvailable = true
            ),
            Song(
                id = "ind_apna_bana_le",
                title = "Apna Bana Le",
                artist = "Arijit Singh & Sachin-Jigar",
                album = "Bhediya",
                duration = 261000,
                artworkUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=600&auto=format&fit=crop",
                streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI,
                sourceProvider = "IndianTopCharts",
                playbackAvailable = true
            ),
            Song(
                id = "ind_raataan_lambiyan",
                title = "Raataan Lambiyan",
                artist = "Jubin Nautiyal & Asees Kaur",
                album = "Shershaah",
                duration = 230000,
                artworkUrl = "https://images.unsplash.com/photo-1511379938547-c1f69419868d?w=600&auto=format&fit=crop",
                streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI,
                sourceProvider = "IndianTopCharts",
                playbackAvailable = true
            ),
            Song(
                id = "ind_tauba_tauba",
                title = "Tauba Tauba",
                artist = "Karan Aujla",
                album = "Bad Newz",
                duration = 208000,
                artworkUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop",
                streamUrl = com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI,
                sourceProvider = "IndianTopCharts",
                playbackAvailable = true
            )
        )
    }

    suspend fun search(context: Context, query: String): YTMusicSearchResult {
        val rawQ = if (query.isBlank()) "Indian hits" else query
        val indianSearchTerm = if (rawQ.lowercase().contains("indian") || rawQ.lowercase().contains("hindi") || rawQ.lowercase().contains("bollywood") || rawQ.lowercase().contains("punjabi")) {
            rawQ
        } else {
            "Indian $rawQ"
        }

        // 1. Query iTunes API with country="IN" for Indian songs
        try {
            val response = PublicMusicApiClient.apiService.searchMusic(term = indianSearchTerm, entity = "song", limit = 30, country = "IN")
            val code = response.code()
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!.results
                if (dtos.isNotEmpty()) {
                    val songs = dtos.map { it.toSong() }

                    val artists = dtos.mapNotNull { dto ->
                        dto.artistName?.let { name ->
                            Artist(
                                id = "artist_${Math.abs(name.hashCode())}",
                                name = name,
                                image = dto.artworkUrl100?.replace("100x100bb", "600x600bb") ?: "",
                                genres = dto.primaryGenreName ?: "Indian Music",
                                followers = 25000
                            )
                        }
                    }.distinctBy { it.name }

                    val albums = dtos.mapNotNull { dto ->
                        dto.collectionName?.let { name ->
                            Album(
                                id = "album_${Math.abs(name.hashCode())}",
                                title = name,
                                artistId = dto.artistName ?: "Artist",
                                artwork = dto.artworkUrl100?.replace("100x100bb", "600x600bb") ?: "",
                                releaseDate = System.currentTimeMillis(),
                                totalTracks = 10
                            )
                        }
                    }.distinctBy { it.title }

                    val playlists = listOf(
                        Playlist(
                            id = "p1",
                            title = "Indian Top Hits",
                            description = "Trending Bollywood, Hindi & Punjabi music",
                            artwork = songs.firstOrNull()?.artworkUrl
                        ),
                        Playlist(
                            id = "p2",
                            title = "Bollywood Romance",
                            description = "Soulful Indian romantic melodies",
                            artwork = songs.getOrNull(1)?.artworkUrl ?: songs.firstOrNull()?.artworkUrl
                        )
                    )

                    SpotifyDiagnostics.recordRequest("itunes_indian_search", code, songs.size)
                    return YTMusicSearchResult(songs = songs, artists = artists, albums = albums, playlists = playlists)
                }
            }
        } catch (e: Exception) {
            Log.w("MusicRepository", "iTunes Indian search attempt error: ${e.localizedMessage}")
        }

        // 2. Try Jamendo API with Indian tags if iTunes API returned empty
        try {
            val jResponse = JamendoApiClient.apiService.getTracks(nameSearch = indianSearchTerm, tags = "indian,bollywood,hindi", limit = 25)
            if (jResponse.isSuccessful && jResponse.body() != null) {
                val jTracks = jResponse.body()!!.results
                if (jTracks.isNotEmpty()) {
                    val songs = jTracks.map { it.toSong() }
                    val artists = jTracks.mapNotNull { dto ->
                        dto.artistName?.let { name ->
                            Artist(
                                id = dto.artistId ?: "artist_${Math.abs(name.hashCode())}",
                                name = name,
                                image = dto.image ?: dto.albumImage ?: "",
                                genres = "Indian Music",
                                followers = 20000
                            )
                        }
                    }.distinctBy { it.name }

                    val albums = jTracks.mapNotNull { dto ->
                        dto.albumName?.let { name ->
                            Album(
                                id = dto.albumId ?: "album_${Math.abs(name.hashCode())}",
                                title = name,
                                artistId = dto.artistName ?: "Artist",
                                artwork = dto.albumImage ?: dto.image ?: "",
                                releaseDate = System.currentTimeMillis(),
                                totalTracks = 8
                            )
                        }
                    }.distinctBy { it.title }

                    val playlists = listOf(
                        Playlist(id = "p1", title = "Indian Indie", description = "Fresh Indian indie tracks", artwork = songs.firstOrNull()?.artworkUrl)
                    )

                    return YTMusicSearchResult(songs = songs, artists = artists, albums = albums, playlists = playlists)
                }
            }
        } catch (e: Exception) {
            Log.w("MusicRepository", "Jamendo search attempt failed: ${e.localizedMessage}")
        }

        // 2. Fallback to iTunes API
        try {
            val response = PublicMusicApiClient.apiService.searchMusic(term = indianSearchTerm, entity = "song", limit = 25, country = "IN")
            val code = response.code()
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()!!.results
                val songs = dtos.map { it.toSong() }

                val artists = dtos.mapNotNull { dto ->
                    dto.artistName?.let { name ->
                        Artist(
                            id = "artist_${Math.abs(name.hashCode())}",
                            name = name,
                            image = dto.artworkUrl100?.replace("100x100bb", "600x600bb") ?: "",
                            genres = dto.primaryGenreName ?: "Music",
                            followers = 10000
                        )
                    }
                }.distinctBy { it.name }

                val albums = dtos.mapNotNull { dto ->
                    dto.collectionName?.let { name ->
                        Album(
                            id = "album_${Math.abs(name.hashCode())}",
                            title = name,
                            artistId = dto.artistName ?: "Artist",
                            artwork = dto.artworkUrl100?.replace("100x100bb", "600x600bb") ?: "",
                            releaseDate = System.currentTimeMillis(),
                            totalTracks = 10
                        )
                    }
                }.distinctBy { it.title }

                val playlists = listOf(
                    Playlist(
                        id = "p1",
                        title = "Top Trending Hits",
                        description = "Hot music tracks right now",
                        artwork = songs.firstOrNull()?.artworkUrl
                    ),
                    Playlist(
                        id = "p2",
                        title = "Relax & Chill",
                        description = "Relaxing songs for every mood",
                        artwork = songs.getOrNull(1)?.artworkUrl ?: songs.firstOrNull()?.artworkUrl
                    )
                )

                SpotifyDiagnostics.recordRequest("search", code, songs.size)

                if (songs.isNotEmpty()) {
                    return YTMusicSearchResult(songs = songs, artists = artists, albums = albums, playlists = playlists)
                }
            } else {
                SpotifyDiagnostics.recordRequest("search", code, 0, "HTTP $code")
            }
        } catch (e: Exception) {
            Log.w("MusicRepository", "Public search error, falling back to local database: ${e.localizedMessage}")
            SpotifyDiagnostics.recordRequest("search", 500, 0, e.localizedMessage)
        }

        val localSongs = songDao.getAllSongs().first()
        if (localSongs.isNotEmpty()) {
            val sampleArtists = localSongs.map { Artist(id = "a_${it.artist}", name = it.artist, image = it.artworkUrl) }.distinctBy { it.name }
            val sampleAlbums = localSongs.map { Album(id = "alb_${it.album}", title = it.album, artistId = it.artist, artwork = it.artworkUrl, releaseDate = 0L, totalTracks = 1) }.distinctBy { it.title }
            return YTMusicSearchResult(songs = localSongs, artists = sampleArtists, albums = sampleAlbums, playlists = emptyList())
        }

        throw Exception("Unable to load music. Please check your network connection.")
    }

    fun getArtistData(context: Context, artistId: String): Flow<Result<YTMusicArtistData>> = flow {
        try {
            val response = PublicMusicApiClient.apiService.searchMusic(term = artistId, entity = "song", limit = 15)
            if (response.isSuccessful && response.body() != null) {
                val songs = response.body()!!.results.map { it.toSong() }
                val artistName = songs.firstOrNull()?.artist ?: artistId
                val artist = Artist(id = artistId, name = artistName, image = songs.firstOrNull()?.artworkUrl ?: "")
                val albums = songs.map { song ->
                    Album(id = "album_${Math.abs(song.album.hashCode())}", title = song.album, artistId = song.artist, artwork = song.artworkUrl, releaseDate = 0L, totalTracks = 1)
                }.distinctBy { it.title }
                emit(Result.success(YTMusicArtistData(artist = artist, songs = songs, albums = albums)))
                return@flow
            }
        } catch (e: Exception) {
            Log.w("MusicRepository", "Get artist data error: ${e.localizedMessage}")
        }

        val localSongs = songDao.getAllSongs().first()
        if (localSongs.isNotEmpty()) {
            val artist = Artist(id = artistId, name = localSongs.first().artist, image = localSongs.first().artworkUrl)
            emit(Result.success(YTMusicArtistData(artist = artist, songs = localSongs, albums = emptyList())))
        } else {
            emit(Result.failure(Exception("Unable to load artist details.")))
        }
    }.flowOn(Dispatchers.IO)

    fun getPlaylistData(context: Context, playlistId: String): Flow<Result<YTMusicPlaylistData>> = flow {
        try {
            val response = PublicMusicApiClient.apiService.searchMusic(term = "hits", entity = "song", limit = 15)
            if (response.isSuccessful && response.body() != null) {
                val songs = response.body()!!.results.map { it.toSong() }
                val playlist = Playlist(id = playlistId, title = "Curated Hits", description = "Trending tracks", artwork = songs.firstOrNull()?.artworkUrl)
                emit(Result.success(YTMusicPlaylistData(playlist = playlist, songs = songs)))
                return@flow
            }
        } catch (e: Exception) {
            Log.w("MusicRepository", "Get playlist data error: ${e.localizedMessage}")
        }

        val localSongs = songDao.getAllSongs().first()
        if (localSongs.isNotEmpty()) {
            val playlist = Playlist(id = playlistId, title = "Saved Playlist", description = "Local music", artwork = localSongs.first().artworkUrl)
            emit(Result.success(YTMusicPlaylistData(playlist = playlist, songs = localSongs)))
        } else {
            emit(Result.failure(Exception("Unable to load playlist.")))
        }
    }.flowOn(Dispatchers.IO)
}

fun JamendoTrackDto.toSong(): Song {
    val artwork = image?.ifBlank { null } ?: albumImage?.ifBlank { null }
        ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop"
    val stream = audio?.ifBlank { null } ?: audioDownload?.ifBlank { null }
        ?: com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI
    return Song(
        id = id ?: UUID.randomUUID().toString(),
        title = name ?: "Unknown Track",
        artist = artistName ?: "Unknown Artist",
        album = albumName ?: "Single",
        duration = (duration ?: 180L) * 1000L,
        artworkUrl = artwork,
        streamUrl = stream,
        sourceProvider = "jamendo",
        playbackAvailable = true
    )
}

fun ITunesTrackDto.toSong(): Song {
    val artwork = artworkUrl100?.replace("100x100bb", "600x600bb")
        ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop"
    return Song(
        id = trackId?.toString() ?: UUID.randomUUID().toString(),
        title = trackName ?: "Unknown Track",
        artist = artistName ?: "Unknown Artist",
        album = collectionName ?: "Single",
        duration = trackTimeMillis ?: 180000L,
        artworkUrl = artwork,
        streamUrl = previewUrl ?: com.example.playback.AudioPlayerManager.OFFICIAL_TEST_AUDIO_URI,
        sourceProvider = "itunes",
        playbackAvailable = !previewUrl.isNullOrBlank()
    )
}

fun SpotifyTrackDto.toSong(): Song {
    val primaryArtist = artists?.firstOrNull()?.name ?: "Unknown Artist"
    val albumName = album?.name ?: "Unknown Album"
    val artwork = album?.images?.firstOrNull()?.url ?: ""
    val duration = durationMs ?: 0L
    return Song(
        id = id,
        title = name,
        artist = primaryArtist,
        album = albumName,
        duration = duration,
        artworkUrl = artwork,
        streamUrl = previewUrl ?: "",
        sourceProvider = "spotify",
        playbackAvailable = !previewUrl.isNullOrBlank()
    )
}

fun SpotifyArtistDto.toArtist(): Artist {
    val img = images?.firstOrNull()?.url ?: ""
    val gen = genres?.joinToString(", ") ?: "Pop"
    return Artist(
        id = id,
        name = name,
        image = img,
        genres = gen,
        followers = popularity ?: 50
    )
}

fun SpotifyAlbumDto.toAlbum(): Album {
    val img = images?.firstOrNull()?.url ?: ""
    val artistId = artists?.firstOrNull()?.id ?: ""
    return Album(
        id = id,
        title = name,
        artistId = artistId,
        artwork = img,
        releaseDate = 0L,
        totalTracks = tracks?.items?.size ?: 10
    )
}

fun SpotifyPlaylistDto.toPlaylist(): Playlist {
    val img = images?.firstOrNull()?.url ?: ""
    val desc = description ?: "Playlist"
    return Playlist(
        id = id,
        title = name,
        description = desc,
        artwork = img
    )
}
