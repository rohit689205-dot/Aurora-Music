package com.example.data.api

import com.example.data.api.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AuroraApiService {

    @GET("api/search")
    suspend fun search(
        @Query("q") query: String
    ): Response<AuroraSearchResultDto>

    @GET("api/songs/{video_id}")
    suspend fun getSong(
        @Path("video_id") videoId: String
    ): Response<AuroraSongDetailDto>

    @GET("api/artists/{artist_id}")
    suspend fun getArtist(
        @Path("artist_id") artistId: String
    ): Response<AuroraArtistDetailDto>

    @GET("api/albums/{album_id}")
    suspend fun getAlbum(
        @Path("album_id") albumId: String
    ): Response<AuroraAlbumDetailDto>

    @GET("api/playlists/{playlist_id}")
    suspend fun getPlaylist(
        @Path("playlist_id") playlistId: String
    ): Response<AuroraPlaylistDetailDto>

    @GET("api/charts")
    suspend fun getCharts(
        @Query("country") country: String = "IN"
    ): Response<List<AuroraItemDto>>

    @GET("api/songs/{video_id}/lyrics")
    suspend fun getLyrics(
        @Path("video_id") videoId: String
    ): Response<AuroraLyricsDto>

    @GET("api/diagnostics")
    suspend fun getDiagnostics(): Response<AuroraDiagnosticsDto>
}
