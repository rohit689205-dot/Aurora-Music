package com.example.data

import android.util.Log
import com.example.data.api.JamendoApiClient
import com.example.data.api.model.JamendoTrackDto
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import java.util.UUID

class JamendoRepository {

    private val apiService = JamendoApiClient.apiService

    suspend fun fetchPopularTracks(limit: Int = 30): List<Song> {
        return try {
            val response = apiService.getPopularTracks(limit = limit)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.results.map { it.toSong() }
            } else {
                Log.e("JamendoRepository", "Error fetching popular tracks: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("JamendoRepository", "Exception fetching popular tracks: ${e.localizedMessage}")
            emptyList()
        }
    }

    suspend fun searchTracks(query: String, limit: Int = 30): List<Song> {
        return try {
            val response = apiService.getTracks(nameSearch = query, limit = limit)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.results.map { it.toSong() }
            } else {
                Log.e("JamendoRepository", "Error searching tracks: ${response.code()}")
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("JamendoRepository", "Exception searching tracks: ${e.localizedMessage}")
            emptyList()
        }
    }

    suspend fun fetchTracksByTag(tag: String, limit: Int = 25): List<Song> {
        return try {
            val response = apiService.getTracks(tags = tag, limit = limit)
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.results.map { it.toSong() }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchJamendoHomeData(query: String = ""): YTMusicSearchResult {
        val tracks = if (query.isNotBlank()) {
            searchTracks(query, limit = 30)
        } else {
            fetchPopularTracks(limit = 30).ifEmpty {
                searchTracks("pop", limit = 30)
            }
        }

        val artists = tracks.map { song ->
            Artist(
                id = "artist_${Math.abs(song.artist.hashCode())}",
                name = song.artist,
                image = song.artworkUrl,
                genres = "Jamendo Popular",
                followers = 15000
            )
        }.distinctBy { it.name }

        val albums = tracks.map { song ->
            Album(
                id = "album_${Math.abs(song.album.hashCode())}",
                title = song.album,
                artistId = song.artist,
                artwork = song.artworkUrl,
                releaseDate = System.currentTimeMillis(),
                totalTracks = 10
            )
        }.distinctBy { it.title }

        val playlists = listOf(
            Playlist(
                id = "p_jamendo_popular",
                title = "Jamendo Trending Hits",
                description = "Popular full-length streaming songs from Jamendo API",
                artwork = tracks.firstOrNull()?.artworkUrl
            ),
            Playlist(
                id = "p_jamendo_discover",
                title = "Discover New Artists",
                description = "Independent music powered by Jamendo API",
                artwork = tracks.getOrNull(1)?.artworkUrl ?: tracks.firstOrNull()?.artworkUrl
            )
        )

        return YTMusicSearchResult(
            songs = tracks,
            artists = artists,
            albums = albums,
            playlists = playlists
        )
    }
}
