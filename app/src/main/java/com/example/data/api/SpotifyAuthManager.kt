package com.example.data.api

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SpotifyDiagnostics {
    private val _spotifyConfigured = MutableStateFlow(true)
    val spotifyConfigured: StateFlow<Boolean> = _spotifyConfigured.asStateFlow()

    private val _apiReachable = MutableStateFlow(true)
    val apiReachable: StateFlow<Boolean> = _apiReachable.asStateFlow()

    private val _lastHttpStatus = MutableStateFlow<Int?>(200)
    val lastHttpStatus: StateFlow<Int?> = _lastHttpStatus.asStateFlow()

    private val _lastEndpoint = MutableStateFlow<String?>("itunes/search")
    val lastEndpoint: StateFlow<String?> = _lastEndpoint.asStateFlow()

    private val _resultCount = MutableStateFlow(0)
    val resultCount: StateFlow<Int> = _resultCount.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun updateConfigured(configured: Boolean) { _spotifyConfigured.value = configured }
    fun updateApiReachable(reachable: Boolean) { _apiReachable.value = reachable }
    fun recordRequest(endpoint: String, status: Int, count: Int = 0, error: String? = null) {
        _lastEndpoint.value = endpoint
        _lastHttpStatus.value = status
        _resultCount.value = count
        _lastError.value = error
        if (status in 200..299) {
            _apiReachable.value = true
        }
    }
}

class SpotifyAuthManager(private val context: Context) {
    fun checkConfiguration(): Boolean = true
    fun isConfigured(): Boolean = true
    suspend fun getAccessToken(): String = "public_access_token"
    fun clearToken() {}
}
