package com.example.data.api

import com.example.data.api.model.YouTubeChannelListResponse
import com.example.data.api.model.YouTubePlaylistItemListResponse
import com.example.data.api.model.YouTubePlaylistListResponse
import com.example.data.api.model.YouTubeSearchResponse
import com.example.data.api.model.YouTubeVideoListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("part") part: String = "snippet",
        @Query("type") type: String? = "video,playlist,channel",
        @Query("videoCategoryId") videoCategoryId: String? = null,
        @Query("maxResults") maxResults: Int = 20,
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String = YouTubeConfig.getApiKey()
    ): Response<YouTubeSearchResponse>

    @GET("videos")
    suspend fun getVideos(
        @Query("id") ids: String,
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("key") apiKey: String = YouTubeConfig.getApiKey()
    ): Response<YouTubeVideoListResponse>

    @GET("videos")
    suspend fun getPopularMusicVideos(
        @Query("chart") chart: String = "mostPopular",
        @Query("videoCategoryId") videoCategoryId: String = "10", // 10 = Music category
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("maxResults") maxResults: Int = 20,
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String = YouTubeConfig.getApiKey()
    ): Response<YouTubeVideoListResponse>

    @GET("playlists")
    suspend fun getPlaylists(
        @Query("id") playlistId: String,
        @Query("part") part: String = "snippet,contentDetails",
        @Query("key") apiKey: String = YouTubeConfig.getApiKey()
    ): Response<YouTubePlaylistListResponse>

    @GET("playlistItems")
    suspend fun getPlaylistItems(
        @Query("playlistId") playlistId: String,
        @Query("part") part: String = "snippet,contentDetails",
        @Query("maxResults") maxResults: Int = 25,
        @Query("pageToken") pageToken: String? = null,
        @Query("key") apiKey: String = YouTubeConfig.getApiKey()
    ): Response<YouTubePlaylistItemListResponse>

    @GET("channels")
    suspend fun getChannelDetails(
        @Query("id") channelId: String,
        @Query("part") part: String = "snippet,statistics,brandingSettings",
        @Query("key") apiKey: String = YouTubeConfig.getApiKey()
    ): Response<YouTubeChannelListResponse>
}
