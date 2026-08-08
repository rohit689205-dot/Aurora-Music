package com.example.data

import android.util.Log
import com.example.data.api.JioSaavnApiClient
import com.example.data.api.YTMusicApiClient
import com.example.data.api.model.InnertubePlayerRequest
import com.example.data.api.model.InnertubeSearchRequest
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

class YTMusicRepository {

    private val apiService = YTMusicApiClient.apiService

    fun search(query: String): Flow<Result<YTMusicSearchResult>> = flow {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            emit(Result.success(YTMusicSearchResult()))
            return@flow
        }

        val result = try {
            // First search on JioSaavn for high quality audio & rich metadata
            val saavnResp = JioSaavnApiClient.apiService.searchSongs(query = trimmed, limit = 15)
            val saavnSongs = mutableListOf<Song>()
            if (saavnResp.isSuccessful && saavnResp.body()?.results != null) {
                saavnResp.body()!!.results!!.forEach { item ->
                    val id = item.id ?: return@forEach
                    val title = item.song?.replace("&quot;", "\"")?.replace("&amp;", "&") ?: "Unknown Track"
                    val artist = item.primaryArtists ?: item.singers ?: "Unknown Artist"
                    val album = item.album ?: "Single"
                    var img = item.image ?: ""
                    img = img.replace("150x150", "500x500").replace("50x50", "500x500")

                    val durationMs = try {
                        (item.duration?.toLong() ?: 180L) * 1000L
                    } catch (e: Exception) {
                        180000L
                    }

                    saavnSongs.add(
                        Song(
                            id = id,
                            title = title,
                            artist = artist,
                            album = album,
                            duration = durationMs,
                            artworkUrl = img,
                            streamUrl = item.mediaPreviewUrl ?: ""
                        )
                    )
                }
            }

            // Next perform Innertube YT Music Search
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

            // Combine JioSaavn + YT Music songs (require non-blank stream URL)
            val combinedSongs = (ytSongs + saavnSongs)
                .filter { it.streamUrl.isNotBlank() }
                .distinctBy { "${it.title.lowercase()}_${it.artist.lowercase()}" }

            // Extract Artists & Albums dynamically
            val artists = combinedSongs.map { it.artist }.distinct().take(5).map { artistName ->
                Artist(
                    id = "artist_${artistName.hashCode()}",
                    name = artistName,
                    image = combinedSongs.firstOrNull { it.artist == artistName }?.artworkUrl ?: ""
                )
            }

            val albums = combinedSongs.map { it.album }.distinct().take(5).map { albumName ->
                val sample = combinedSongs.firstOrNull { it.album == albumName }
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
                    songs = combinedSongs,
                    artists = artists,
                    albums = albums,
                    playlists = emptyList()
                )
            )

        } catch (e: Exception) {
            Log.e("YTMusicRepository", "Search error", e)
            Result.failure(e)
        }

        emit(result)
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
                                streamUrl = ""
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
        val playlist = Playlist(id = playlistId, title = "Echo Top Charts", description = "Trending YouTube Music tracks")
        emit(Result.success(YTMusicPlaylistData(playlist = playlist, items = emptyList())))
    }

    fun getArtistData(artistId: String): Flow<Result<YTMusicArtistData>> = flow {
        val artist = Artist(id = artistId, name = "Featured Artist", image = "")
        emit(Result.success(YTMusicArtistData(artist = artist, topSongs = emptyList(), albums = emptyList())))
    }
}
