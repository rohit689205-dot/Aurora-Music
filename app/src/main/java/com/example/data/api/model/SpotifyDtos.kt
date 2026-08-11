package com.example.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SpotifyTokenResponseDto(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Long,
    @Json(name = "refresh_token") val refreshToken: String?
)

@JsonClass(generateAdapter = true)
data class SpotifyImageDto(
    @Json(name = "url") val url: String,
    @Json(name = "height") val height: Int?,
    @Json(name = "width") val width: Int?
)

@JsonClass(generateAdapter = true)
data class SpotifyArtistDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "images") val images: List<SpotifyImageDto>?,
    @Json(name = "genres") val genres: List<String>?,
    @Json(name = "popularity") val popularity: Int?,
    @Json(name = "external_urls") val externalUrls: Map<String, String>?
)

@JsonClass(generateAdapter = true)
data class SpotifyAlbumDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "images") val images: List<SpotifyImageDto>?,
    @Json(name = "release_date") val releaseDate: String?,
    @Json(name = "artists") val artists: List<SpotifyArtistDto>?,
    @Json(name = "tracks") val tracks: SpotifyTracksPageDto?,
    @Json(name = "external_urls") val externalUrls: Map<String, String>?
)

@JsonClass(generateAdapter = true)
data class SpotifyTracksPageDto(
    @Json(name = "items") val items: List<SpotifyTrackDto>?
)

@JsonClass(generateAdapter = true)
data class SpotifyTrackDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "artists") val artists: List<SpotifyArtistDto>?,
    @Json(name = "album") val album: SpotifyAlbumDto?,
    @Json(name = "duration_ms") val durationMs: Long?,
    @Json(name = "preview_url") val previewUrl: String?,
    @Json(name = "external_urls") val externalUrls: Map<String, String>?
)

@JsonClass(generateAdapter = true)
data class SpotifyPlaylistTrackItemDto(
    @Json(name = "track") val track: SpotifyTrackDto?
)

@JsonClass(generateAdapter = true)
data class SpotifyPlaylistTracksDto(
    @Json(name = "items") val items: List<SpotifyPlaylistTrackItemDto>?
)

@JsonClass(generateAdapter = true)
data class SpotifyPlaylistDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "description") val description: String?,
    @Json(name = "images") val images: List<SpotifyImageDto>?,
    @Json(name = "owner") val owner: SpotifyOwnerDto?,
    @Json(name = "tracks") val tracks: SpotifyPlaylistTracksDto?,
    @Json(name = "external_urls") val externalUrls: Map<String, String>?
)

@JsonClass(generateAdapter = true)
data class SpotifyOwnerDto(
    @Json(name = "display_name") val displayName: String?
)

@JsonClass(generateAdapter = true)
data class SpotifyPagingDto<T>(
    @Json(name = "items") val items: List<T>?
)

@JsonClass(generateAdapter = true)
data class SpotifySearchResponseDto(
    @Json(name = "tracks") val tracks: SpotifyPagingDto<SpotifyTrackDto>?,
    @Json(name = "artists") val artists: SpotifyPagingDto<SpotifyArtistDto>?,
    @Json(name = "albums") val albums: SpotifyPagingDto<SpotifyAlbumDto>?,
    @Json(name = "playlists") val playlists: SpotifyPagingDto<SpotifyPlaylistDto>?
)

@JsonClass(generateAdapter = true)
data class SpotifyAlbumsResponseDto(
    @Json(name = "items") val items: List<SpotifyAlbumDto>?
)

@JsonClass(generateAdapter = true)
data class SpotifyNewReleasesDto(
    @Json(name = "albums") val albums: SpotifyPagingDto<SpotifyAlbumDto>?
)

@JsonClass(generateAdapter = true)
data class SpotifyCategoryDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "icons") val icons: List<SpotifyImageDto>?
)

@JsonClass(generateAdapter = true)
data class SpotifyCategoriesWrapperDto(
    @Json(name = "categories") val categories: SpotifyPagingDto<SpotifyCategoryDto>?
)
