package com.example.data

import android.util.Log
import com.example.data.api.AuroraApiClient
import com.example.data.api.YTMusicApiClient
import com.example.data.api.model.AuroraItemDto
import com.example.data.api.model.InnertubePlayerRequest
import com.example.data.api.model.InnertubeSearchRequest
import com.example.data.providers.MusicProvider
import com.example.data.providers.ProviderCapabilities
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class YTMusicSearchResult(
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val playlists: List<Playlist> = emptyList()
)

data class YTMusicPlaylistData(
    val playlist: Playlist,
    val items: List<Song>
)

data class YTMusicAlbumData(
    val album: Album,
    val items: List<Song>
)

data class YTMusicArtistData(
    val artist: Artist,
    val topSongs: List<Song>,
    val albums: List<Album>
)

class YTMusicRepository : MusicProvider {

    override val providerName: String = "YouTubeMusic"
    override val capabilities: ProviderCapabilities = ProviderCapabilities(
        search = true,
        artists = true,
        albums = true,
        playlists = true,
        lyrics = true,
        playback = false
    )

    private val apiService = YTMusicApiClient.apiService

    override suspend fun search(query: String): Result<YTMusicSearchResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.success(YTMusicSearchResult())
        }

        // 1. Try FastAPI backend (ytmusicapi)
        try {
            val auroraResp = AuroraApiClient.apiService.search(trimmed)
            if (auroraResp.isSuccessful && auroraResp.body() != null) {
                val dto = auroraResp.body()!!
                
                val rawSongs: List<AuroraItemDto> = if (dto.songs.isNotEmpty()) dto.songs else dto.results.filter { it.type == "song" }
                val songs: List<Song> = rawSongs.map { item ->
                    Song(
                        id = item.id,
                        title = item.title,
                        artist = item.artist,
                        album = item.album ?: "Single",
                        duration = 210000L,
                        artworkUrl = item.thumbnail ?: "",
                        streamUrl = item.streamUrl ?: "",
                        playbackAvailable = item.playbackAvailable,
                        sourceProvider = "YouTubeMusic (ytmusicapi)",
                        isPreview = false
                    )
                }

                val rawArtists: List<AuroraItemDto> = if (dto.artists.isNotEmpty()) dto.artists else dto.results.filter { it.type == "artist" }
                val artists: List<Artist> = rawArtists.map { item ->
                    Artist(
                        id = item.id,
                        name = if (item.title.isNotBlank()) item.title else item.artist,
                        image = item.thumbnail ?: ""
                    )
                }

                val rawAlbums: List<AuroraItemDto> = if (dto.albums.isNotEmpty()) dto.albums else dto.results.filter { it.type == "album" }
                val albums: List<Album> = rawAlbums.map { item ->
                    Album(
                        id = item.id,
                        title = item.title,
                        artistId = "artist_${item.artist.hashCode()}",
                        artwork = item.thumbnail ?: "",
                        releaseDate = System.currentTimeMillis(),
                        totalTracks = 10
                    )
                }

                val rawPlaylists: List<AuroraItemDto> = if (dto.playlists.isNotEmpty()) dto.playlists else dto.results.filter { it.type == "playlist" }
                val playlists: List<Playlist> = rawPlaylists.map { item ->
                    Playlist(
                        id = item.id,
                        title = item.title,
                        description = "YouTube Music Playlist"
                    )
                }

                if (songs.isNotEmpty() || artists.isNotEmpty() || albums.isNotEmpty() || playlists.isNotEmpty()) {
                    return@withContext Result.success(
                        YTMusicSearchResult(
                            songs = songs,
                            artists = artists,
                            albums = albums,
                            playlists = playlists
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w("YTMusicRepository", "Aurora Backend query failed, falling back to InnerTube: ${e.localizedMessage}")
        }

        // 2. Direct InnerTube fallback
        try {
            val ytRequest = InnertubeSearchRequest(
                context = YTMusicApiClient.createSearchContext(),
                query = trimmed
            )
            val ytResp = apiService.search(ytRequest)
            val ytSongs = mutableListOf<Song>()

            if (ytResp.isSuccessful && ytResp.body() != null) {
                val jsonString = ytResp.body()!!.string()
                val parsedYtSongs = parseInnertubeSearch(jsonString)
                ytSongs.addAll(parsedYtSongs)
            }

            val artists = ytSongs.map { it.artist }.distinct().take(5).map { artistName ->
                Artist(
                    id = "artist_${artistName.hashCode()}",
                    name = artistName,
                    image = ytSongs.firstOrNull { it.artist == artistName }?.artworkUrl ?: ""
                )
            }

            val albums = ytSongs.map { it.album }.distinct().take(5).map { albumName ->
                val sample = ytSongs.firstOrNull { it.album == albumName }
                Album(
                    id = "album_${albumName.hashCode()}",
                    title = albumName,
                    artistId = "artist_${sample?.artist?.hashCode()}",
                    artwork = sample?.artworkUrl ?: "",
                    releaseDate = System.currentTimeMillis(),
                    totalTracks = 10
                )
            }

            Result.success(
                YTMusicSearchResult(
                    songs = ytSongs,
                    artists = artists,
                    albums = albums,
                    playlists = emptyList()
                )
            )

        } catch (e: Exception) {
            Log.e("YTMusicRepository", "InnerTube Search error", e)
            Result.failure(e)
        }
    }

    override suspend fun getCharts(category: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val resp = AuroraApiClient.apiService.getCharts("IN")
            if (resp.isSuccessful && !resp.body().isNullOrEmpty()) {
                val songs = resp.body()!!.map { item ->
                    Song(
                        id = item.id,
                        title = item.title,
                        artist = item.artist,
                        album = "Top Chart",
                        duration = 210000L,
                        artworkUrl = item.thumbnail ?: "",
                        streamUrl = item.streamUrl ?: "",
                        playbackAvailable = item.playbackAvailable,
                        sourceProvider = "YouTubeMusic (ytmusicapi)"
                    )
                }
                return@withContext Result.success(songs)
            }
        } catch (e: Exception) {
            Log.w("YTMusicRepository", "Charts query to backend failed, falling back to search: ${e.localizedMessage}")
        }
        search(category).map { it.songs }
    }

    suspend fun getStreamUrlForVideo(videoId: String): String? {
        return try {
            val req = InnertubePlayerRequest(
                context = YTMusicApiClient.createPlayerContext(),
                videoId = videoId
            )
            val resp = apiService.getPlayer(req)
            if (resp.isSuccessful && resp.body() != null) {
                val body = resp.body()!!
                val formats = body.streamingData?.adaptiveFormats ?: body.streamingData?.formats
                val audioStream = formats?.firstOrNull { f ->
                    (f.mimeType?.contains("audio", ignoreCase = true) == true) && !f.url.isNullOrBlank()
                } ?: formats?.firstOrNull { f -> !f.url.isNullOrBlank() }

                audioStream?.url
            } else null
        } catch (e: Exception) {
            Log.e("YTMusicRepository", "Error getting audio stream for $videoId", e)
            null
        }
    }

    private fun parseInnertubeSearch(jsonStr: String): List<Song> {
        val songs = mutableListOf<Song>()
        try {
            val root = JSONObject(jsonStr)
            val contents = root.optJSONObject("contents")
                ?.optJSONObject("tabbedSearchResultsRenderer")
                ?.optJSONArray("tabs")
                ?.optJSONObject(0)
                ?.optJSONObject("tabRenderer")
                ?.optJSONObject("content")
                ?.optJSONObject("sectionListRenderer")
                ?.optJSONArray("contents") ?: return songs

            for (i in 0 until contents.length()) {
                val section = contents.optJSONObject(i)
                val shelf = section?.optJSONObject("musicShelfRenderer") ?: continue
                val items = shelf.optJSONArray("contents") ?: continue

                for (j in 0 until items.length()) {
                    val item = items.optJSONObject(j)?.optJSONObject("musicResponsiveListItemRenderer") ?: continue
                    val flexColumns = item.optJSONArray("flexColumns") ?: continue

                    // Title
                    var title = "Unknown Track"
                    val col0 = flexColumns.optJSONObject(0)
                        ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                        ?.optJSONObject("text")
                        ?.optJSONArray("runs")
                    if (col0 != null && col0.length() > 0) {
                        title = col0.optJSONObject(0)?.optString("text") ?: "Unknown Track"
                    }

                    // Artist & Album
                    var artist = "YT Music Artist"
                    var album = "Single"
                    val col1 = flexColumns.optJSONObject(1)
                        ?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                        ?.optJSONObject("text")
                        ?.optJSONArray("runs")
                    if (col1 != null && col1.length() > 0) {
                        artist = col1.optJSONObject(0)?.optString("text") ?: artist
                        if (col1.length() >= 3) {
                            album = col1.optJSONObject(2)?.optString("text") ?: album
                        }
                    }

                    // Video ID / Song ID
                    val playNavigation = item.optJSONObject("overlay")
                        ?.optJSONObject("musicItemThumbnailOverlayRenderer")
                        ?.optJSONObject("content")
                        ?.optJSONObject("musicPlayButtonRenderer")
                        ?.optJSONObject("playNavigationEndpoint")
                        ?.optJSONObject("watchEndpoint")
                    val videoId = playNavigation?.optString("videoId") ?: ""

                    // Thumbnail
                    val thumbnails = item.optJSONObject("thumbnail")
                        ?.optJSONObject("musicThumbnailRenderer")
                        ?.optJSONObject("thumbnail")
                        ?.optJSONArray("thumbnails")
                    var thumbUrl = ""
                    if (thumbnails != null && thumbnails.length() > 0) {
                        thumbUrl = thumbnails.optJSONObject(thumbnails.length() - 1)?.optString("url") ?: ""
                    }

                    if (videoId.isNotBlank()) {
                        songs.add(
                            Song(
                                id = videoId,
                                title = title,
                                artist = artist,
                                album = album,
                                duration = 210000L,
                                artworkUrl = thumbUrl,
                                streamUrl = "",
                                playbackAvailable = false,
                                sourceProvider = "YouTubeMusic",
                                isPreview = false
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("YTMusicRepository", "Error parsing Innertube search JSON", e)
        }
        return songs
    }

    fun getAlbumData(albumId: String): Flow<Result<YTMusicAlbumData>> = flow {
        try {
            val resp = AuroraApiClient.apiService.getAlbum(albumId)
            if (resp.isSuccessful && resp.body() != null) {
                val detail = resp.body()!!
                val album = Album(
                    id = detail.id,
                    title = detail.title,
                    artistId = "artist_${detail.artist.hashCode()}",
                    artwork = detail.thumbnail ?: "",
                    releaseDate = System.currentTimeMillis(),
                    totalTracks = detail.tracks.size
                )
                val songs = detail.tracks.map { item ->
                    Song(
                        id = item.id,
                        title = item.title,
                        artist = item.artist,
                        album = detail.title,
                        duration = 210000L,
                        artworkUrl = item.thumbnail ?: "",
                        streamUrl = item.streamUrl ?: "",
                        playbackAvailable = item.playbackAvailable,
                        sourceProvider = "YouTubeMusic (ytmusicapi)"
                    )
                }
                emit(Result.success(YTMusicAlbumData(album = album, items = songs)))
                return@flow
            }
        } catch (e: Exception) {
            Log.w("YTMusicRepository", "Failed to get album data from backend: ${e.localizedMessage}")
        }

        val album = Album(
            id = albumId,
            title = "Trending Album",
            artistId = "featured_artist",
            artwork = "",
            releaseDate = System.currentTimeMillis(),
            totalTracks = 10
        )
        emit(Result.success(YTMusicAlbumData(album = album, items = emptyList())))
    }

    fun getPlaylistData(playlistId: String): Flow<Result<YTMusicPlaylistData>> = flow {
        try {
            val resp = AuroraApiClient.apiService.getPlaylist(playlistId)
            if (resp.isSuccessful && resp.body() != null) {
                val detail = resp.body()!!
                val playlist = Playlist(
                    id = detail.id,
                    title = detail.title,
                    description = detail.description ?: "YouTube Music Playlist"
                )
                val songs = detail.tracks.map { item ->
                    Song(
                        id = item.id,
                        title = item.title,
                        artist = item.artist,
                        album = detail.title,
                        duration = 210000L,
                        artworkUrl = item.thumbnail ?: "",
                        streamUrl = item.streamUrl ?: "",
                        playbackAvailable = item.playbackAvailable,
                        sourceProvider = "YouTubeMusic (ytmusicapi)"
                    )
                }
                emit(Result.success(YTMusicPlaylistData(playlist = playlist, items = songs)))
                return@flow
            }
        } catch (e: Exception) {
            Log.w("YTMusicRepository", "Failed to get playlist data from backend: ${e.localizedMessage}")
        }

        val playlist = Playlist(id = playlistId, title = "Echo Top Charts", description = "Trending YouTube Music tracks")
        emit(Result.success(YTMusicPlaylistData(playlist = playlist, items = emptyList())))
    }

    fun getArtistData(artistId: String): Flow<Result<YTMusicArtistData>> = flow {
        try {
            val resp = AuroraApiClient.apiService.getArtist(artistId)
            if (resp.isSuccessful && resp.body() != null) {
                val detail = resp.body()!!
                val artist = Artist(
                    id = detail.id,
                    name = detail.name,
                    image = detail.thumbnail ?: ""
                )
                val topSongs = detail.songs.map { item ->
                    Song(
                        id = item.id,
                        title = item.title,
                        artist = detail.name,
                        album = item.album ?: "Popular Track",
                        duration = 210000L,
                        artworkUrl = item.thumbnail ?: "",
                        streamUrl = item.streamUrl ?: "",
                        playbackAvailable = item.playbackAvailable,
                        sourceProvider = "YouTubeMusic (ytmusicapi)"
                    )
                }
                val albums = detail.albums.map { item ->
                    Album(
                        id = item.id,
                        title = item.title,
                        artistId = detail.id,
                        artwork = item.thumbnail ?: "",
                        releaseDate = System.currentTimeMillis(),
                        totalTracks = 10
                    )
                }
                emit(Result.success(YTMusicArtistData(artist = artist, topSongs = topSongs, albums = albums)))
                return@flow
            }
        } catch (e: Exception) {
            Log.w("YTMusicRepository", "Failed to get artist data from backend: ${e.localizedMessage}")
        }

        val artist = Artist(id = artistId, name = "Featured Artist", image = "")
        emit(Result.success(YTMusicArtistData(artist = artist, topSongs = emptyList(), albums = emptyList())))
    }
}
