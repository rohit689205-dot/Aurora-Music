package com.example.data.api

import com.example.data.api.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SpotifyApiService {
    @GET("v1/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String = "track,artist,album,playlist",
        @Query("limit") limit: Int = 20
    ): Response<SpotifySearchResponseDto>

    @GET("v1/tracks/{id}")
    suspend fun getTrack(
        @Path("id") id: String
    ): Response<SpotifyTrackDto>

    @GET("v1/artists/{id}")
    suspend fun getArtist(
        @Path("id") id: String
    ): Response<SpotifyArtistDto>

    @GET("v1/artists/{id}/albums")
    suspend fun getArtistAlbums(
        @Path("id") id: String,
        @Query("limit") limit: Int = 20
    ): Response<SpotifyAlbumsResponseDto>

    @GET("v1/albums/{id}")
    suspend fun getAlbum(
        @Path("id") id: String
    ): Response<SpotifyAlbumDto>

    @GET("v1/playlists/{id}")
    suspend fun getPlaylist(
        @Path("id") id: String
    ): Response<SpotifyPlaylistDto>

    @GET("v1/browse/new-releases")
    suspend fun getNewReleases(
        @Query("limit") limit: Int = 20
    ): Response<SpotifyNewReleasesDto>

    @GET("v1/browse/categories")
    suspend fun getCategories(
        @Query("limit") limit: Int = 20
    ): Response<SpotifyCategoriesWrapperDto>
}
