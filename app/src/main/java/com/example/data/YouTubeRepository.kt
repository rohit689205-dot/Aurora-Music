package com.example.data

import com.example.data.api.YouTubeApiClient
import com.example.data.api.YouTubeConfig
import com.example.data.api.YouTubeMapper
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

data class YouTubeSearchResult(
    val songs: List<Song> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val nextPageToken: String? = null,
    val totalResults: Int = 0
)

data class YouTubePlaylistData(
    val playlist: Playlist,
    val items: List<Song> = emptyList(),
    val nextPageToken: String? = null
)

data class YouTubeArtistData(
    val artist: Artist,
    val bannerUrl: String? = null,
    val subscriberText: String = "",
    val topSongs: List<Song> = emptyList(),
    val nextPageToken: String? = null
)

class YouTubeRepository(
    private val apiService: com.example.data.api.YouTubeApiService = YouTubeApiClient.apiService
) {

    fun search(
        query: String,
        musicCategoryOnly: Boolean = false,
        pageToken: String? = null
    ): Flow<Result<YouTubeSearchResult>> = flow {
        if (!YouTubeConfig.isKeyConfigured()) {
            emit(Result.failure(IllegalStateException("YouTube API key is missing or not configured.")))
            return@flow
        }

        try {
            val categoryParam = if (musicCategoryOnly) "10" else null
            val response = apiService.search(
                query = query,
                videoCategoryId = categoryParam,
                pageToken = pageToken
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val items = body.items ?: emptyList()

                val videoIds = items.mapNotNull { it.id?.videoId }
                val videoDetailMap = if (videoIds.isNotEmpty()) {
                    val videoDetailsResponse = apiService.getVideos(ids = videoIds.joinToString(","))
                    if (videoDetailsResponse.isSuccessful) {
                        videoDetailsResponse.body()?.items?.associateBy { it.id } ?: emptyMap()
                    } else emptyMap()
                } else emptyMap()

                val songs = mutableListOf<Song>()
                val playlists = mutableListOf<Playlist>()
                val artists = mutableListOf<Artist>()

                items.forEach { item ->
                    when (item.id?.kind) {
                        "youtube#video" -> {
                            val vId = item.id.videoId
                            val detailedVideo = videoDetailMap[vId]
                            if (detailedVideo != null) {
                                songs.add(YouTubeMapper.videoItemToSong(detailedVideo))
                            } else {
                                YouTubeMapper.searchItemToSong(item)?.let { songs.add(it) }
                            }
                        }
                        "youtube#playlist" -> {
                            YouTubeMapper.searchItemToPlaylist(item)?.let { playlists.add(it) }
                        }
                        "youtube#channel" -> {
                            YouTubeMapper.searchItemToArtist(item)?.let { artists.add(it) }
                        }
                    }
                }

                emit(
                    Result.success(
                        YouTubeSearchResult(
                            songs = songs,
                            playlists = playlists,
                            artists = artists,
                            nextPageToken = body.nextPageToken,
                            totalResults = body.pageInfo?.totalResults ?: (songs.size + playlists.size + artists.size)
                        )
                    )
                )
            } else {
                val errorMsg = parseErrorMessage(response.code(), response.errorBody()?.string())
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getPopularMusic(pageToken: String? = null): Flow<Result<Pair<List<Song>, String?>>> = flow {
        if (!YouTubeConfig.isKeyConfigured()) {
            emit(Result.failure(IllegalStateException("YouTube API key is missing.")))
            return@flow
        }

        try {
            val response = apiService.getPopularMusicVideos(pageToken = pageToken)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val songs = (body.items ?: emptyList()).map { YouTubeMapper.videoItemToSong(it) }
                emit(Result.success(Pair(songs, body.nextPageToken)))
            } else {
                val errorMsg = parseErrorMessage(response.code(), response.errorBody()?.string())
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getPlaylistData(playlistId: String, pageToken: String? = null): Flow<Result<YouTubePlaylistData>> = flow {
        if (!YouTubeConfig.isKeyConfigured()) {
            emit(Result.failure(IllegalStateException("YouTube API key is missing.")))
            return@flow
        }

        try {
            val playlistResponse = apiService.getPlaylists(playlistId = playlistId)
            val playlist = if (playlistResponse.isSuccessful && !playlistResponse.body()?.items.isNullOrEmpty()) {
                YouTubeMapper.playlistItemToPlaylist(playlistResponse.body()!!.items!![0])
            } else {
                Playlist(id = playlistId, title = "YouTube Playlist", description = "YouTube Playlist Tracks")
            }

            val itemsResponse = apiService.getPlaylistItems(playlistId = playlistId, pageToken = pageToken)
            if (itemsResponse.isSuccessful && itemsResponse.body() != null) {
                val body = itemsResponse.body()!!
                val rawItems = body.items ?: emptyList()
                val videoIds = rawItems.mapNotNull { it.snippet?.resourceId?.videoId ?: it.contentDetails?.videoId }

                val songDetailsMap = if (videoIds.isNotEmpty()) {
                    val vResp = apiService.getVideos(ids = videoIds.joinToString(","))
                    if (vResp.isSuccessful) {
                        vResp.body()?.items?.associateBy { it.id } ?: emptyMap()
                    } else emptyMap()
                } else emptyMap()

                val songs = rawItems.map { rawItem ->
                    val vId = rawItem.snippet?.resourceId?.videoId ?: rawItem.contentDetails?.videoId
                    val detailedVideo = songDetailsMap[vId]
                    if (detailedVideo != null) {
                        YouTubeMapper.videoItemToSong(detailedVideo)
                    } else {
                        YouTubeMapper.playlistItemToSong(rawItem)
                    }
                }

                emit(Result.success(YouTubePlaylistData(playlist = playlist, items = songs, nextPageToken = body.nextPageToken)))
            } else {
                val errorMsg = parseErrorMessage(itemsResponse.code(), itemsResponse.errorBody()?.string())
                emit(Result.failure(Exception(errorMsg)))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    fun getArtistData(channelIdOrQuery: String): Flow<Result<YouTubeArtistData>> = flow {
        if (!YouTubeConfig.isKeyConfigured()) {
            emit(Result.failure(IllegalStateException("YouTube API key is missing.")))
            return@flow
        }

        try {
            var channelId = channelIdOrQuery
            var artist: Artist? = null
            var bannerUrl: String? = null
            var subText = "Verified Channel"

            if (!channelId.startsWith("UC")) {
                val searchResponse = apiService.search(query = channelIdOrQuery, type = "channel", maxResults = 1)
                if (searchResponse.isSuccessful && !searchResponse.body()?.items.isNullOrEmpty()) {
                    val searchItem = searchResponse.body()!!.items!![0]
                    channelId = searchItem.id?.channelId ?: channelIdOrQuery
                    artist = YouTubeMapper.searchItemToArtist(searchItem)
                }
            }

            val channelResponse = apiService.getChannelDetails(channelId = channelId)
            if (channelResponse.isSuccessful && !channelResponse.body()?.items.isNullOrEmpty()) {
                val channelItem = channelResponse.body()!!.items!![0]
                artist = YouTubeMapper.channelItemToArtist(channelItem)
                bannerUrl = channelItem.brandingSettings?.image?.bannerExternalUrl
                subText = YouTubeMapper.formatSubscriberCount(channelItem.statistics?.subscriberCount)
            }

            val videoSearchResp = apiService.search(query = artist?.name ?: channelIdOrQuery, type = "video", maxResults = 15)
            val topSongs = mutableListOf<Song>()
            var nextPageToken: String? = null

            if (videoSearchResp.isSuccessful && videoSearchResp.body() != null) {
                val body = videoSearchResp.body()!!
                nextPageToken = body.nextPageToken
                val videoIds = (body.items ?: emptyList()).mapNotNull { it.id?.videoId }
                if (videoIds.isNotEmpty()) {
                    val detailsResp = apiService.getVideos(ids = videoIds.joinToString(","))
                    if (detailsResp.isSuccessful && detailsResp.body()?.items != null) {
                        topSongs.addAll(detailsResp.body()!!.items!!.map { YouTubeMapper.videoItemToSong(it) })
                    }
                }
            }

            val finalArtist = artist ?: Artist(id = channelId, name = channelIdOrQuery, image = "", verified = true)
            emit(
                Result.success(
                    YouTubeArtistData(
                        artist = finalArtist,
                        bannerUrl = bannerUrl,
                        subscriberText = subText,
                        topSongs = topSongs,
                        nextPageToken = nextPageToken
                    )
                )
            )
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }.flowOn(Dispatchers.IO)

    private fun parseErrorMessage(code: Int, errorBody: String?): String {
        return when {
            code == 403 || errorBody?.contains("quotaExceeded", ignoreCase = true) == true ->
                "YouTube API quota exceeded or unauthorized access. Please try again later."
            code == 400 || errorBody?.contains("API key not valid", ignoreCase = true) == true ->
                "Invalid YouTube API key. Please check your configuration."
            code == 404 -> "Requested YouTube content not found."
            else -> "YouTube API error ($code): ${errorBody?.take(100) ?: "Unknown network failure"}"
        }
    }
}
