package com.example.data.api

import com.example.BuildConfig

object YouTubeConfig {
    const val BASE_URL = "https://www.googleapis.com/youtube/v3/"
    
    fun getApiKey(): String {
        return try {
            val key = BuildConfig.YOUTUBE_API_KEY
            if (key.isBlank() || key == "MY_YOUTUBE_API_KEY") {
                ""
            } else {
                key
            }
        } catch (e: Exception) {
            ""
        }
    }
    
    fun isKeyConfigured(): Boolean {
        return getApiKey().isNotBlank()
    }
}
