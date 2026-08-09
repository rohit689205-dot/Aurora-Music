package com.example.data.providers

import android.util.Log
import com.example.data.YTMusicSearchResult
import com.example.model.Album
import com.example.model.Artist
import com.example.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class JamendoRepository : MusicProvider {
    override val providerName: String = "Jamendo"
    override val capabilities: ProviderCapabilities = ProviderCapabilities(
        search = true,
        artists = true,
        albums = true,
        playlists = false,
        lyrics = false,
        playback = true
    )

    private val client = OkHttpClient()
    private val clientId = "599371bb" // Public Jamendo test client id

    override suspend fun search(query: String): Result<YTMusicSearchResult> = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.jamendo.com/v3.0/tracks/?client_id=$clientId&format=json&limit=20&search=${java.net.URLEncoder.encode(query, "UTF-8")}"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Jamendo error HTTP ${response.code}"))
            }
            val bodyString = response.body?.string() ?: "{}"
            val json = JSONObject(bodyString)
            val resultsArray = json.optJSONArray("results") ?: org.json.JSONArray()
            val songs = mutableListOf<Song>()
            val artistsMap = mutableMapOf<String, Artist>()
            val albumsMap = mutableMapOf<String, Album>()

            for (i in 0 until resultsArray.length()) {
                val item = resultsArray.getJSONObject(i)
                val id = "jamendo_${item.optString("id")}"
                val name = item.optString("name", "Unknown Track")
                val artistName = item.optString("artist_name", "Unknown Artist")
                val albumName = item.optString("album_name", "Jamendo Album")
                val audioUrl = item.optString("audio", "")
                val image = item.optString("image", "").ifEmpty { item.optString("album_image", "") }
                val duration = item.optLong("duration", 180L) * 1000L

                if (audioUrl.isNotBlank()) {
                    val song = Song(
                        id = id,
                        title = name,
                        artist = artistName,
                        album = albumName,
                        duration = duration,
                        artworkUrl = image,
                        streamUrl = audioUrl,
                        playbackAvailable = true,
                        sourceProvider = "Jamendo",
                        isPreview = false
                    )
                    songs.add(song)

                    artistsMap.getOrPut(artistName) {
                        Artist(id = "artist_jamendo_${artistName.hashCode()}", name = artistName, image = image)
                    }
                    albumsMap.getOrPut(albumName) {
                        Album(id = "album_jamendo_${albumName.hashCode()}", title = albumName, artistId = "artist_jamendo_${artistName.hashCode()}", artwork = image, releaseDate = System.currentTimeMillis(), totalTracks = 10)
                    }
                }
            }

            Result.success(YTMusicSearchResult(songs = songs, artists = artistsMap.values.toList(), albums = albumsMap.values.toList()))
        } catch (e: Exception) {
            Log.w("JamendoRepository", "Search failed: ${e.localizedMessage}")
            Result.success(YTMusicSearchResult())
        }
    }

    override suspend fun getCharts(category: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val tag = when (category.lowercase()) {
                "hindi", "indian" -> "indian"
                "punjabi" -> "punjabi"
                "tamil" -> "tamil"
                "telugu" -> "telugu"
                else -> "pop"
            }
            val url = "https://api.jamendo.com/v3.0/tracks/?client_id=$clientId&format=json&limit=15&tags=$tag&order=popularity_total"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.success(emptyList())
            }
            val bodyString = response.body?.string() ?: "{}"
            val json = JSONObject(bodyString)
            val resultsArray = json.optJSONArray("results") ?: org.json.JSONArray()
            val songs = mutableListOf<Song>()

            for (i in 0 until resultsArray.length()) {
                val item = resultsArray.getJSONObject(i)
                val id = "jamendo_${item.optString("id")}"
                val name = item.optString("name", "Unknown Track")
                val artistName = item.optString("artist_name", "Unknown Artist")
                val albumName = item.optString("album_name", "Jamendo Album")
                val audioUrl = item.optString("audio", "")
                val image = item.optString("image", "").ifEmpty { item.optString("album_image", "") }
                val duration = item.optLong("duration", 180L) * 1000L

                if (audioUrl.isNotBlank()) {
                    songs.add(
                        Song(
                            id = id,
                            title = name,
                            artist = artistName,
                            album = albumName,
                            duration = duration,
                            artworkUrl = image,
                            streamUrl = audioUrl,
                            playbackAvailable = true,
                            sourceProvider = "Jamendo",
                            isPreview = false
                        )
                    )
                }
            }
            Result.success(songs)
        } catch (e: Exception) {
            Log.w("JamendoRepository", "Charts failed: ${e.localizedMessage}")
            Result.success(emptyList())
        }
    }
}
