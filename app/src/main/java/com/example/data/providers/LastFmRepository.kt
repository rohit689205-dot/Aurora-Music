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

class LastFmRepository : MusicProvider {
    override val providerName: String = "Last.fm"
    override val capabilities: ProviderCapabilities = ProviderCapabilities(
        search = true,
        artists = true,
        albums = true,
        playlists = false,
        lyrics = false,
        playback = false
    )

    private val client = OkHttpClient()
    private val apiKey = "d3869273c5dce4539efc5b2a05cf6f08" // public last.fm api key demo

    override suspend fun search(query: String): Result<YTMusicSearchResult> = withContext(Dispatchers.IO) {
        try {
            val url = "https://ws.audioscrobbler.com/2.0/?method=track.search&track=${java.net.URLEncoder.encode(query, "UTF-8")}&api_key=$apiKey&format=json&limit=15"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("Last.fm error HTTP ${response.code}"))
            }
            val bodyString = response.body?.string() ?: "{}"
            val json = JSONObject(bodyString)
            val resultsObj = json.optJSONObject("results") ?: JSONObject()
            val trackMatches = resultsObj.optJSONObject("trackmatches") ?: JSONObject()
            val trackArray = trackMatches.optJSONArray("track") ?: org.json.JSONArray()

            val songs = mutableListOf<Song>()
            val artistsMap = mutableMapOf<String, Artist>()

            for (i in 0 until trackArray.length()) {
                val item = trackArray.getJSONObject(i)
                val name = item.optString("name", "Unknown Track")
                val artist = item.optString("artist", "Unknown Artist")
                val id = "lastfm_${name.hashCode()}_${artist.hashCode()}"
                val imageArray = item.optJSONArray("image")
                var imageUrl = ""
                if (imageArray != null && imageArray.length() > 0) {
                    imageUrl = imageArray.getJSONObject(imageArray.length() - 1).optString("#text", "")
                }

                songs.add(
                    Song(
                        id = id,
                        title = name,
                        artist = artist,
                        album = "Last.fm Discovery",
                        duration = 200000L,
                        artworkUrl = imageUrl.ifEmpty { "https://picsum.photos/seed/$id/500/500" },
                        streamUrl = "", // No direct stream
                        playbackAvailable = false,
                        sourceProvider = "Last.fm",
                        isPreview = false
                    )
                )

                artistsMap.getOrPut(artist) {
                    Artist(id = "artist_lastfm_${artist.hashCode()}", name = artist, image = imageUrl)
                }
            }

            Result.success(YTMusicSearchResult(songs = songs, artists = artistsMap.values.toList()))
        } catch (e: Exception) {
            Log.w("LastFmRepository", "Search failed: ${e.localizedMessage}")
            Result.success(YTMusicSearchResult())
        }
    }

    override suspend fun getCharts(category: String): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val url = "https://ws.audioscrobbler.com/2.0/?method=chart.gettoptracks&api_key=$apiKey&format=json&limit=15"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.success(emptyList())
            }
            val bodyString = response.body?.string() ?: "{}"
            val json = JSONObject(bodyString)
            val tracksObj = json.optJSONObject("tracks") ?: JSONObject()
            val trackArray = tracksObj.optJSONArray("track") ?: org.json.JSONArray()
            val songs = mutableListOf<Song>()

            for (i in 0 until trackArray.length()) {
                val item = trackArray.getJSONObject(i)
                val name = item.optString("name", "Unknown Track")
                val artistObj = item.optJSONObject("artist")
                val artist = artistObj?.optString("name", "Unknown Artist") ?: "Unknown Artist"
                val id = "lastfm_chart_${name.hashCode()}_${artist.hashCode()}"
                val imageArray = item.optJSONArray("image")
                var imageUrl = ""
                if (imageArray != null && imageArray.length() > 0) {
                    imageUrl = imageArray.getJSONObject(imageArray.length() - 1).optString("#text", "")
                }

                songs.add(
                    Song(
                        id = id,
                        title = name,
                        artist = artist,
                        album = "Last.fm Top Tracks",
                        duration = 210000L,
                        artworkUrl = imageUrl.ifEmpty { "https://picsum.photos/seed/$id/500/500" },
                        streamUrl = "",
                        playbackAvailable = false,
                        sourceProvider = "Last.fm",
                        isPreview = false
                    )
                )
            }
            Result.success(songs)
        } catch (e: Exception) {
            Log.w("LastFmRepository", "Charts failed: ${e.localizedMessage}")
            Result.success(emptyList())
        }
    }
}
