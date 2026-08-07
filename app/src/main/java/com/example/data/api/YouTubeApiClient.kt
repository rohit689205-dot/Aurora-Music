package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object YouTubeApiClient {

    private const val CONNECT_TIMEOUT_SEC = 15L
    private const val READ_TIMEOUT_SEC = 20L
    private const val MAX_RETRIES = 3

    private class TransientErrorRetryInterceptor : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            var response: Response? = null
            var exception: IOException? = null
            var tryCount = 0

            while (tryCount < MAX_RETRIES) {
                try {
                    response?.close()
                    response = chain.proceed(request)
                    if (response.isSuccessful || response.code !in 500..599) {
                        return response
                    }
                } catch (e: IOException) {
                    exception = e
                }
                tryCount++
                if (tryCount < MAX_RETRIES) {
                    try {
                        Thread.sleep(1000L * tryCount)
                    } catch (_: InterruptedException) {
                        Thread.currentThread().interrupt()
                        break
                    }
                }
            }

            if (response != null) {
                return response
            }
            throw exception ?: IOException("Failed request after $MAX_RETRIES attempts")
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }

        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(TransientErrorRetryInterceptor())
            .build()
    }

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    val apiService: YouTubeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(YouTubeConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(YouTubeApiService::class.java)
    }
}
