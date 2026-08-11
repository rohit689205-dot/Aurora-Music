package com.example.data.api.model

import com.squareup.moshi.Json

data class ITunesSearchResponse(
    @Json(name = "resultCount") val resultCount: Int = 0,
    @Json(name = "results") val results: List<ITunesTrackDto> = emptyList()
)

data class ITunesTrackDto(
    @Json(name = "trackId") val trackId: Long? = null,
    @Json(name = "trackName") val trackName: String? = null,
    @Json(name = "artistName") val artistName: String? = null,
    @Json(name = "collectionName") val collectionName: String? = null,
    @Json(name = "artworkUrl100") val artworkUrl100: String? = null,
    @Json(name = "previewUrl") val previewUrl: String? = null,
    @Json(name = "trackTimeMillis") val trackTimeMillis: Long? = null,
    @Json(name = "primaryGenreName") val primaryGenreName: String? = null
)

data class JamendoResponse(
    @Json(name = "headers") val headers: JamendoHeaderDto? = null,
    @Json(name = "results") val results: List<JamendoTrackDto> = emptyList()
)

data class JamendoHeaderDto(
    @Json(name = "status") val status: String? = null,
    @Json(name = "code") val code: Int? = null,
    @Json(name = "results_count") val resultsCount: Int? = null
)

data class JamendoTrackDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "duration") val duration: Long? = null,
    @Json(name = "artist_id") val artistId: String? = null,
    @Json(name = "artist_name") val artistName: String? = null,
    @Json(name = "album_id") val albumId: String? = null,
    @Json(name = "album_name") val albumName: String? = null,
    @Json(name = "album_image") val albumImage: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "audio") val audio: String? = null,
    @Json(name = "audiodownload") val audioDownload: String? = null
)

