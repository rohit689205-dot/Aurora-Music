package com.example.data.api

import android.util.Log
import com.example.data.api.model.InnertubeClient
import com.example.data.api.model.InnertubeContext
import com.example.data.api.model.InnertubePlayerRequest
import com.example.data.api.model.InnertubeSearchRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object YTMusicApiClient {

    private const val TAG = "YTMusicApiClient"
    private const val BASE_URL = "https://music.youtube.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: YTMusicApiService = retrofit.create(YTMusicApiService::class.java)

    fun createSearchContext(): InnertubeContext {
        return InnertubeContext(
            client = InnertubeClient(
                clientName = "WEB_REMIX",
                clientVersion = "1.20231214.00.00"
            )
        )
    }

    fun createPlayerContext(): InnertubeContext {
        return InnertubeContext(
            client = InnertubeClient(
                clientName = "ANDROID",
                clientVersion = "19.05.36",
                androidSdkVersion = 30
            )
        )
    }
}
