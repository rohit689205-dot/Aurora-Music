package com.example.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JioSaavnSearchResponse(
    @Json(name = "total") val total: Int? = null,
    @Json(name = "start") val start: Int? = null,
    @Json(name = "results") val results: List<JioSaavnSongItem>? = null
)

@JsonClass(generateAdapter = true)
data class JioSaavnSongItem(
    @Json(name = "id") val id: String? = null,
    @Json(name = "song") val song: String? = null,
    @Json(name = "album") val album: String? = null,
    @Json(name = "year") val year: String? = null,
    @Json(name = "primary_artists") val primaryArtists: String? = null,
    @Json(name = "singers") val singers: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "duration") val duration: String? = null,
    @Json(name = "has_lyrics") val hasLyrics: String? = null,
    @Json(name = "lyrics_snippet") val lyricsSnippet: String? = null,
    @Json(name = "media_preview_url") val mediaPreviewUrl: String? = null,
    @Json(name = "perma_url") val permaUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class JioSaavnAutocompleteResponse(
    @Json(name = "albums") val albums: JioSaavnSection? = null,
    @Json(name = "songs") val songs: JioSaavnSection? = null,
    @Json(name = "artists") val artists: JioSaavnSection? = null,
    @Json(name = "playlists") val playlists: JioSaavnSection? = null
)

@JsonClass(generateAdapter = true)
data class JioSaavnSection(
    @Json(name = "data") val data: List<JioSaavnSongItem>? = null
)
