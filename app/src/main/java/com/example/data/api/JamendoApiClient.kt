package com.example.data.api

import com.example.BuildConfig
import com.example.data.api.model.JamendoResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface JamendoApiService {
    @GET("tracks/")
    suspend fun getTracks(
        @Query("client_id") clientId: String = CLIENT_ID,
        @Query("format") format: String = "json",
        @Query("namesearch") nameSearch: String? = null,
        @Query("fuzzytags") tags: String? = null,
        @Query("limit") limit: Int = 25,
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("order") order: String = "popularity_week"
    ): Response<JamendoResponse>

    @GET("tracks/")
    suspend fun getPopularTracks(
        @Query("client_id") clientId: String = CLIENT_ID,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 30,
        @Query("audioformat") audioFormat: String = "mp32",
        @Query("order") order: String = "popularity_total"
    ): Response<JamendoResponse>

    companion object {
        const val CLIENT_SECRET = "660f57bd9198cc19a45d5fe84c5e4651"
        val CLIENT_ID: String
            get() = try {
                val key = BuildConfig.JAMENDO_CLIENT_ID
                if (key.isNotBlank() && key != "none") key else "907de42f"
            } catch (e: Exception) {
                "907de42f"
            }
    }
}

object JamendoApiClient {
    private const val BASE_URL = "https://api.jamendo.com/v3.0/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: JamendoApiService = retrofit.create(JamendoApiService::class.java)
}
