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

class AudiusRepository : MusicProvider {
    override val providerName: String = "Audius"
    override val capabilities: ProviderCapabilities = ProviderCapabilities(
        search = true,
        artists = true,
        albums = false,
        playlists = true,
        lyrics = false,
        playback = true
    )

    private val client = OkHttpClient()
    private val appName = "AuroraMusic"

    override suspend fun search(query: String): Result<YTMusicSearchResult> = withContext(Dispatchers.IO) {
        try {
            val url = "https://discovery-provider.audius.co/v1/tracks/search?query=${java.net.URLEncoder.encode(query, "UTF-8")}&app_name=$appName"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Audius error HTTP ${response.code}"))
            }
            val bodyString = response.body?.string() ?: "{}"
            val json = JSONObject(bodyString)
            val dataArray = json.optJSONArray("data") ?: org.json.JSONArray()
            val songs = mutableListOf<Song>()
            val artistsMap = mutableMapOf<String, Artist>()

            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val id = item.optString("id")
                val title = item.optString("title", "Unknown Track")
                val user = item.optJSONObject("user")
                val artistName = user?.optString("name", "Unknown Artist") ?: "Unknown Artist"
                val artworkObj = item.optJSONObject("artwork")
                val artworkUrl = artworkObj?.optString("480x480", "") ?: ""
                val duration = item.optLong("duration", 180L) * 1000L
                val streamUrl = "https://discovery-provider.audius.co/v1/tracks/$id/stream?app_name=$appName"

                val song = Song(
                    id = "audius_$id",
                    title = title,
                    artist = artistName,
                    album = "Audius Release",
                    duration = duration,
                    artworkUrl = artworkUrl.ifEmpty { "https://picsum.photos/seed/$id/500/500" },
                    streamUrl = streamUrl,
                    playbackAvailable = true,
                    sourceProvider = "Audius",
                    isPreview = false
                )
                songs.add(song)

                artistsMap.getOrPut(artistName) {
                    Artist(id = "artist_audius_${artistName.hashCode()}", name = artistName, image = artworkUrl)
                }
            }

            Result.success(YTMusicSearchResult(songs = songs, artists = artistsMap.values.toList()))
        } catch (e: Exception) {
            Log.w("AudiusRepository", "Search failed: ${e.localizedMessage}")
            Result.success(YTMusicSearchResult())
        }
    }

    override suspend fun getCharts(category: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val url = "https://discovery-provider.audius.co/v1/tracks/trending?app_name=$appName"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.success(emptyList())
            }
            val bodyString = response.body?.string() ?: "{}"
            val json = JSONObject(bodyString)
            val dataArray = json.optJSONArray("data") ?: org.json.JSONArray()
            val songs = mutableListOf<Song>()

            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val id = item.optString("id")
                val title = item.optString("title", "Unknown Track")
                val user = item.optJSONObject("user")
                val artistName = user?.optString("name", "Unknown Artist") ?: "Unknown Artist"
                val artworkObj = item.optJSONObject("artwork")
                val artworkUrl = artworkObj?.optString("480x480", "") ?: ""
                val duration = item.optLong("duration", 180L) * 1000L
                val streamUrl = "https://discovery-provider.audius.co/v1/tracks/$id/stream?app_name=$appName"

                songs.add(
                    Song(
                        id = "audius_$id",
                        title = title,
                        artist = artistName,
                        album = "Audius Trending",
                        duration = duration,
                        artworkUrl = artworkUrl.ifEmpty { "https://picsum.photos/seed/$id/500/500" },
                        streamUrl = streamUrl,
                        playbackAvailable = true,
                        sourceProvider = "Audius",
                        isPreview = false
                    )
                )
            }
            Result.success(songs)
        } catch (e: Exception) {
            Log.w("AudiusRepository", "Charts failed: ${e.localizedMessage}")
            Result.success(emptyList())
        }
    }
}
