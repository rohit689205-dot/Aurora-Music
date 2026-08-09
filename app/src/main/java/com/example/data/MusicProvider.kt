package com.example.data

import kotlinx.coroutines.flow.Flow

interface MusicProvider {
    fun search(query: String): Flow<Result<YTMusicSearchResult>>
}
