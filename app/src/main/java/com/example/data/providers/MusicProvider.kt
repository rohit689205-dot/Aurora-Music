package com.example.data.providers

import com.example.data.YTMusicSearchResult
import com.example.model.Song
import kotlinx.coroutines.flow.Flow

interface MusicProvider {
    val providerName: String
    val capabilities: ProviderCapabilities
    suspend fun search(query: String): Result<YTMusicSearchResult>
    suspend fun getCharts(category: String): Result<List<Song>>
}
