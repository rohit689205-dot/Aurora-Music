package com.example.data.providers

import com.example.data.YTMusicRepository
import com.example.data.YTMusicSearchResult
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import kotlinx.coroutines.coroutineScope

class UnifiedMusicSearchRepository(
    private val ytMusicRepository: YTMusicRepository = YTMusicRepository()
) {
    suspend fun searchAll(query: String): YTMusicSearchResult = coroutineScope {
        val q = query.ifBlank { "Arijit Singh" }
        val result = ytMusicRepository.search(q)
        if (result.isFailure) {
            throw result.exceptionOrNull() ?: Exception("Failed to search music")
        }
        result.getOrThrow()
    }

    suspend fun getHomeCharts(category: String): List<Song> = coroutineScope {
        val result = ytMusicRepository.getCharts(category)
        result.getOrNull() ?: emptyList()
    }
}
