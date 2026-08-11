package com.example.data

import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song

data class YTMusicSearchResult(
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val playlists: List<Playlist> = emptyList()
)

data class YTMusicPlaylistData(
    val playlist: Playlist,
    val songs: List<Song>
) {
    val items: List<Song> get() = songs
}

data class YTMusicArtistData(
    val artist: Artist,
    val songs: List<Song>,
    val albums: List<Album>
) {
    val topSongs: List<Song> get() = songs
}
