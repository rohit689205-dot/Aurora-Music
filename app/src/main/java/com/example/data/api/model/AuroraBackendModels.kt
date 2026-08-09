package com.example.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuroraSearchResultDto(
    @Json(name = "results") val results: List<AuroraItemDto> = emptyList(),
    @Json(name = "songs") val songs: List<AuroraItemDto> = emptyList(),
    @Json(name = "artists") val artists: List<AuroraItemDto> = emptyList(),
    @Json(name = "albums") val albums: List<AuroraItemDto> = emptyList(),
    @Json(name = "playlists") val playlists: List<AuroraItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AuroraItemDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "artist") val artist: String = "",
    @Json(name = "album") val album: String? = "",
    @Json(name = "thumbnail") val thumbnail: String? = "",
    @Json(name = "duration") val duration: String? = "",
    @Json(name = "type") val type: String = "song",
    @Json(name = "provider") val provider: String = "ytmusic",
    @Json(name = "playbackAvailable") val playbackAvailable: Boolean = false,
    @Json(name = "streamUrl") val streamUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class AuroraSongDetailDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "artist") val artist: String = "",
    @Json(name = "artists") val artists: List<String> = emptyList(),
    @Json(name = "album") val album: String? = "",
    @Json(name = "thumbnail") val thumbnail: String? = "",
    @Json(name = "duration") val duration: String? = "",
    @Json(name = "provider") val provider: String = "ytmusic",
    @Json(name = "playbackAvailable") val playbackAvailable: Boolean = false,
    @Json(name = "streamUrl") val streamUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class AuroraArtistDetailDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "thumbnail") val thumbnail: String? = "",
    @Json(name = "description") val description: String? = "",
    @Json(name = "songs") val songs: List<AuroraItemDto> = emptyList(),
    @Json(name = "albums") val albums: List<AuroraItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AuroraAlbumDetailDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "artist") val artist: String = "",
    @Json(name = "thumbnail") val thumbnail: String? = "",
    @Json(name = "year") val year: String? = "",
    @Json(name = "tracks") val tracks: List<AuroraItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AuroraPlaylistDetailDto(
    @Json(name = "id") val id: String = "",
    @Json(name = "title") val title: String = "",
    @Json(name = "description") val description: String? = "",
    @Json(name = "thumbnail") val thumbnail: String? = "",
    @Json(name = "author") val author: String? = "",
    @Json(name = "tracks") val tracks: List<AuroraItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AuroraLyricsDto(
    @Json(name = "lyrics") val lyrics: String? = null,
    @Json(name = "available") val available: Boolean = false
)

@JsonClass(generateAdapter = true)
data class AuroraDiagnosticsDto(
    @Json(name = "backendStatus") val backendStatus: String = "offline",
    @Json(name = "lastRequest") val lastRequest: String = "",
    @Json(name = "httpStatus") val httpStatus: Int = 200,
    @Json(name = "resultCount") val resultCount: Int = 0,
    @Json(name = "searchLatencyMs") val searchLatencyMs: Double = 0.0,
    @Json(name = "lastError") val lastError: String? = null,
    @Json(name = "currentProvider") val currentProvider: String = "ytmusicapi"
)
