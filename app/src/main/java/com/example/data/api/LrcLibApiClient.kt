package com.example.data.api

import android.util.Log
import com.example.data.api.model.LrcLibResponse
import com.example.data.api.model.LyricLine
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object LrcLibApiClient {

    private const val TAG = "LrcLibApiClient"
    private const val BASE_URL = "https://lrclib.net/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: LrcLibApiService = retrofit.create(LrcLibApiService::class.java)

    suspend fun fetchLyrics(trackName: String, artistName: String): LrcLibResponse? {
        try {
            // First try exact get
            val exactResponse = apiService.getLyrics(trackName = trackName, artistName = artistName)
            if (exactResponse.isSuccessful && exactResponse.body() != null) {
                return exactResponse.body()
            }

            // Fallback to search query
            val query = "$trackName $artistName".trim()
            val searchResponse = apiService.searchLyrics(query = query)
            if (searchResponse.isSuccessful) {
                val list = searchResponse.body()
                if (!list.isNullOrEmpty()) {
                    // Prefer item with synced lyrics
                    return list.firstOrNull { !it.syncedLyrics.isNullOrBlank() } ?: list.first()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch lyrics from LRCLIB API", e)
        }
        return null
    }

    fun parseSyncedLyrics(lrcContent: String?): List<LyricLine> {
        if (lrcContent.isNullOrBlank()) return emptyList()

        val result = mutableListOf<LyricLine>()
        // Regex for standard LRC timestamps: [mm:ss.xx] or [mm:ss:xx] or [mm:ss.xxx]
        val timePattern = Pattern.compile("\\[(\\d{2}):(\\d{2})[.:](\\d{2,3})\\](.*)")

        lrcContent.lines().forEach { line ->
            val matcher = timePattern.matcher(line.trim())
            if (matcher.find()) {
                try {
                    val minutes = matcher.group(1)?.toLong() ?: 0L
                    val seconds = matcher.group(2)?.toLong() ?: 0L
                    val fractionStr = matcher.group(3) ?: "0"
                    val millis = if (fractionStr.length == 2) {
                        fractionStr.toLong() * 10
                    } else {
                        fractionStr.toLong()
                    }
                    val text = matcher.group(4)?.trim() ?: ""

                    val timestampMs = (minutes * 60 * 1000) + (seconds * 1000) + millis
                    if (text.isNotBlank()) {
                        result.add(LyricLine(timestampMs, text))
                    }
                } catch (e: Exception) {
                    // Ignore line parse errors
                }
            }
        }

        return result.sortedBy { it.timestampMs }
    }
}
