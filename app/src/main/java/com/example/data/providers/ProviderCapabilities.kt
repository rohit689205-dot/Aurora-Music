package com.example.data.providers

data class ProviderCapabilities(
    val search: Boolean = true,
    val artists: Boolean = true,
    val albums: Boolean = true,
    val playlists: Boolean = true,
    val lyrics: Boolean = false,
    val playback: Boolean = false
)
