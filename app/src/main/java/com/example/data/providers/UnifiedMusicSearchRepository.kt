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
        if (query.isBlank()) {
            val chartResult = ytMusicRepository.getCharts("Trending").getOrNull() ?: emptyList()
            return@coroutineScope YTMusicSearchResult(
                songs = chartResult,
                artists = emptyList(),
                albums = emptyList(),
                playlists = emptyList()
            )
        }

        val result = ytMusicRepository.search(query)
        result.getOrNull() ?: YTMusicSearchResult()
    }

    suspend fun getHomeCharts(category: String): List<Song> = coroutineScope {
        val result = ytMusicRepository.getCharts(category)
        result.getOrNull() ?: emptyList()
    }
}
