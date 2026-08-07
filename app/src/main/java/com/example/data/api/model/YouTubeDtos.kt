package com.example.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YouTubeSearchResponse(
    val kind: String? = null,
    val etag: String? = null,
    val nextPageToken: String? = null,
    val prevPageToken: String? = null,
    val pageInfo: PageInfo? = null,
    val items: List<SearchResultItem>? = null,
    val error: YouTubeApiErrorDetails? = null
)

@JsonClass(generateAdapter = true)
data class SearchResultItem(
    val kind: String? = null,
    val etag: String? = null,
    val id: SearchItemId? = null,
    val snippet: Snippet? = null
)

@JsonClass(generateAdapter = true)
data class SearchItemId(
    val kind: String? = null,
    val videoId: String? = null,
    val playlistId: String? = null,
    val channelId: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeVideoListResponse(
    val kind: String? = null,
    val etag: String? = null,
    val nextPageToken: String? = null,
    val pageInfo: PageInfo? = null,
    val items: List<VideoItem>? = null,
    val error: YouTubeApiErrorDetails? = null
)

@JsonClass(generateAdapter = true)
data class VideoItem(
    val kind: String? = null,
    val etag: String? = null,
    val id: String? = null,
    val snippet: Snippet? = null,
    val contentDetails: VideoContentDetails? = null,
    val statistics: VideoStatistics? = null
)

@JsonClass(generateAdapter = true)
data class VideoContentDetails(
    val duration: String? = null,
    val dimension: String? = null,
    val definition: String? = null,
    val caption: String? = null,
    val licensedContent: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class VideoStatistics(
    val viewCount: String? = null,
    val likeCount: String? = null,
    val favoriteCount: String? = null,
    val commentCount: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistListResponse(
    val kind: String? = null,
    val etag: String? = null,
    val nextPageToken: String? = null,
    val pageInfo: PageInfo? = null,
    val items: List<PlaylistItem>? = null,
    val error: YouTubeApiErrorDetails? = null
)

@JsonClass(generateAdapter = true)
data class PlaylistItem(
    val kind: String? = null,
    val etag: String? = null,
    val id: String? = null,
    val snippet: Snippet? = null,
    val contentDetails: PlaylistContentDetails? = null
)

@JsonClass(generateAdapter = true)
data class PlaylistContentDetails(
    val itemCount: Int? = null
)

@JsonClass(generateAdapter = true)
data class YouTubePlaylistItemListResponse(
    val kind: String? = null,
    val etag: String? = null,
    val nextPageToken: String? = null,
    val pageInfo: PageInfo? = null,
    val items: List<PlaylistItemDetails>? = null,
    val error: YouTubeApiErrorDetails? = null
)

@JsonClass(generateAdapter = true)
data class PlaylistItemDetails(
    val kind: String? = null,
    val etag: String? = null,
    val id: String? = null,
    val snippet: Snippet? = null,
    val contentDetails: PlaylistItemContentDetails? = null
)

@JsonClass(generateAdapter = true)
data class PlaylistItemContentDetails(
    val videoId: String? = null,
    val videoPublishedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeChannelListResponse(
    val kind: String? = null,
    val etag: String? = null,
    val items: List<ChannelItem>? = null,
    val error: YouTubeApiErrorDetails? = null
)

@JsonClass(generateAdapter = true)
data class ChannelItem(
    val kind: String? = null,
    val etag: String? = null,
    val id: String? = null,
    val snippet: Snippet? = null,
    val statistics: ChannelStatistics? = null,
    val brandingSettings: ChannelBrandingSettings? = null
)

@JsonClass(generateAdapter = true)
data class ChannelStatistics(
    val viewCount: String? = null,
    val subscriberCount: String? = null,
    val hiddenSubscriberCount: Boolean? = null,
    val videoCount: String? = null
)

@JsonClass(generateAdapter = true)
data class ChannelBrandingSettings(
    val channel: ChannelBrandingInfo? = null,
    val image: ChannelImageInfo? = null
)

@JsonClass(generateAdapter = true)
data class ChannelBrandingInfo(
    val title: String? = null,
    val description: String? = null,
    val keywords: String? = null
)

@JsonClass(generateAdapter = true)
data class ChannelImageInfo(
    val bannerExternalUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class Snippet(
    val publishedAt: String? = null,
    val channelId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val thumbnails: ThumbnailMap? = null,
    val channelTitle: String? = null,
    val liveBroadcastContent: String? = null,
    val categoryId: String? = null,
    val resourceId: ResourceId? = null
)

@JsonClass(generateAdapter = true)
data class ResourceId(
    val kind: String? = null,
    val videoId: String? = null,
    val playlistId: String? = null,
    val channelId: String? = null
)

@JsonClass(generateAdapter = true)
data class ThumbnailMap(
    val default: ThumbnailDetails? = null,
    val medium: ThumbnailDetails? = null,
    val high: ThumbnailDetails? = null,
    val standard: ThumbnailDetails? = null,
    val maxres: ThumbnailDetails? = null
)

@JsonClass(generateAdapter = true)
data class ThumbnailDetails(
    val url: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

@JsonClass(generateAdapter = true)
data class PageInfo(
    val totalResults: Int? = null,
    val resultsPerPage: Int? = null
)

@JsonClass(generateAdapter = true)
data class YouTubeApiErrorDetails(
    val code: Int? = null,
    val message: String? = null,
    val errors: List<SingleErrorDetail>? = null
)

@JsonClass(generateAdapter = true)
data class SingleErrorDetail(
    val message: String? = null,
    val domain: String? = null,
    val reason: String? = null
)
