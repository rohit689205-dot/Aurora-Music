package com.example.data.api

import com.example.data.api.model.ChannelItem
import com.example.data.api.model.PlaylistItem
import com.example.data.api.model.PlaylistItemDetails
import com.example.data.api.model.SearchResultItem
import com.example.data.api.model.ThumbnailMap
import com.example.data.api.model.VideoItem
import com.example.model.Artist
import com.example.model.Playlist
import com.example.model.Song
import java.util.Locale

object YouTubeMapper {

    fun getBestThumbnailUrl(thumbnails: ThumbnailMap?): String {
        return thumbnails?.maxres?.url
            ?: thumbnails?.high?.url
            ?: thumbnails?.standard?.url
            ?: thumbnails?.medium?.url
            ?: thumbnails?.default?.url
            ?: ""
    }

    fun parseIsoDurationToMillis(durationStr: String?): Long {
        if (durationStr.isNullOrEmpty()) return 0L
        return try {
            var duration = durationStr.replace("PT", "")
            var hours = 0L
            var minutes = 0L
            var seconds = 0L
            if (duration.contains("H")) {
                val parts = duration.split("H")
                hours = parts[0].toLongOrNull() ?: 0L
                duration = parts.getOrNull(1) ?: ""
            }
            if (duration.contains("M")) {
                val parts = duration.split("M")
                minutes = parts[0].toLongOrNull() ?: 0L
                duration = parts.getOrNull(1) ?: ""
            }
            if (duration.contains("S")) {
                val parts = duration.split("S")
                seconds = parts[0].toLongOrNull() ?: 0L
            }
            (hours * 3600 + minutes * 60 + seconds) * 1000L
        } catch (e: Exception) {
            0L
        }
    }

    fun formatViewCount(viewCountStr: String?): String {
        val count = viewCountStr?.toLongOrNull() ?: return ""
        return when {
            count >= 1_000_000_000 -> String.format(Locale.US, "%.1fB views", count / 1_000_000_000.0)
            count >= 1_000_000 -> String.format(Locale.US, "%.1fM views", count / 1_000_000.0)
            count >= 1_000 -> String.format(Locale.US, "%.1fK views", count / 1_000.0)
            else -> "$count views"
        }
    }

    fun formatSubscriberCount(subCountStr: String?): String {
        val count = subCountStr?.toLongOrNull() ?: return "Verified Artist"
        return when {
            count >= 1_000_000_000 -> String.format(Locale.US, "%.1fB subscribers", count / 1_000_000_000.0)
            count >= 1_000_000 -> String.format(Locale.US, "%.1fM subscribers", count / 1_000_000.0)
            count >= 1_000 -> String.format(Locale.US, "%.1fK subscribers", count / 1_000.0)
            else -> "$count subscribers"
        }
    }

    fun videoItemToSong(item: VideoItem): Song {
        val videoId = item.id ?: ""
        val snippet = item.snippet
        val durationMs = parseIsoDurationToMillis(item.contentDetails?.duration)
        return Song(
            id = videoId,
            title = snippet?.title ?: "Unknown Title",
            artist = snippet?.channelTitle ?: "Unknown Artist",
            artistId = snippet?.channelId ?: "",
            album = "YouTube Music",
            duration = durationMs,
            artworkUrl = getBestThumbnailUrl(snippet?.thumbnails),
            streamUrl = "https://www.youtube.com/watch?v=$videoId",
            genre = "Music"
        )
    }

    fun searchItemToSong(item: SearchResultItem): Song? {
        val videoId = item.id?.videoId ?: return null
        val snippet = item.snippet
        return Song(
            id = videoId,
            title = snippet?.title ?: "Unknown Title",
            artist = snippet?.channelTitle ?: "Unknown Artist",
            artistId = snippet?.channelId ?: "",
            album = "YouTube Music",
            duration = 180000L, // Default duration if detail not fetched yet
            artworkUrl = getBestThumbnailUrl(snippet?.thumbnails),
            streamUrl = "https://www.youtube.com/watch?v=$videoId",
            genre = "Music"
        )
    }

    fun searchItemToPlaylist(item: SearchResultItem): Playlist? {
        val playlistId = item.id?.playlistId ?: return null
        val snippet = item.snippet
        return Playlist(
            id = playlistId,
            title = snippet?.title ?: "YouTube Playlist",
            description = snippet?.description,
            artwork = getBestThumbnailUrl(snippet?.thumbnails),
            owner = snippet?.channelTitle ?: "YouTube"
        )
    }

    fun searchItemToArtist(item: SearchResultItem): Artist? {
        val channelId = item.id?.channelId ?: return null
        val snippet = item.snippet
        return Artist(
            id = channelId,
            name = snippet?.title ?: "YouTube Channel",
            image = getBestThumbnailUrl(snippet?.thumbnails),
            biography = snippet?.description,
            verified = true
        )
    }

    fun playlistItemToSong(item: PlaylistItemDetails): Song {
        val videoId = item.snippet?.resourceId?.videoId ?: item.contentDetails?.videoId ?: item.id ?: ""
        val snippet = item.snippet
        return Song(
            id = videoId,
            title = snippet?.title ?: "Unknown Song",
            artist = snippet?.channelTitle ?: "Unknown Artist",
            artistId = snippet?.channelId ?: "",
            album = "YouTube Playlist Track",
            duration = 200000L,
            artworkUrl = getBestThumbnailUrl(snippet?.thumbnails),
            streamUrl = "https://www.youtube.com/watch?v=$videoId"
        )
    }

    fun playlistItemToPlaylist(item: PlaylistItem): Playlist {
        val snippet = item.snippet
        return Playlist(
            id = item.id ?: "",
            title = snippet?.title ?: "YouTube Playlist",
            description = snippet?.description,
            artwork = getBestThumbnailUrl(snippet?.thumbnails),
            owner = snippet?.channelTitle ?: "YouTube Channel"
        )
    }

    fun channelItemToArtist(item: ChannelItem): Artist {
        val snippet = item.snippet
        val stats = item.statistics
        return Artist(
            id = item.id ?: "",
            name = snippet?.title ?: "YouTube Artist",
            image = getBestThumbnailUrl(snippet?.thumbnails),
            biography = snippet?.description ?: item.brandingSettings?.channel?.description,
            followers = stats?.subscriberCount?.toIntOrNull() ?: 0,
            verified = true
        )
    }
}
