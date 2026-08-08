package com.example.data.api

import com.example.data.api.model.LrcLibResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LrcLibApiService {

    @GET("api/get")
    suspend fun getLyrics(
        @Query("track_name") trackName: String,
        @Query("artist_name") artistName: String,
        @Query("album_name") albumName: String? = null,
        @Query("duration") duration: Int? = null
    ): Response<LrcLibResponse>

    @GET("api/search")
    suspend fun searchLyrics(
        @Query("q") query: String
    ): Response<List<LrcLibResponse>>
}
