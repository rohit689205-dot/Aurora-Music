package com.example.data.api

import com.example.data.api.model.InnertubePlayerRequest
import com.example.data.api.model.InnertubePlayerResponse
import com.example.data.api.model.InnertubeSearchRequest
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface YTMusicApiService {

    @Headers("Content-Type: application/json")
    @POST("youtubei/v1/search?alt=json")
    suspend fun search(
        @Body request: InnertubeSearchRequest
    ): Response<ResponseBody>

    @Headers("Content-Type: application/json")
    @POST("youtubei/v1/player?alt=json")
    suspend fun getPlayer(
        @Body request: InnertubePlayerRequest
    ): Response<InnertubePlayerResponse>
}
