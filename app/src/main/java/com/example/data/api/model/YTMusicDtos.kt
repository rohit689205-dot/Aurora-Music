package com.example.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InnertubeSearchRequest(
    @Json(name = "context") val context: InnertubeContext,
    @Json(name = "query") val query: String,
    @Json(name = "params") val params: String? = null
)

@JsonClass(generateAdapter = true)
data class InnertubePlayerRequest(
    @Json(name = "context") val context: InnertubeContext,
    @Json(name = "videoId") val videoId: String
)

@JsonClass(generateAdapter = true)
data class InnertubeContext(
    @Json(name = "client") val client: InnertubeClient
)

@JsonClass(generateAdapter = true)
data class InnertubeClient(
    @Json(name = "clientName") val clientName: String = "WEB_REMIX",
    @Json(name = "clientVersion") val clientVersion: String = "1.20231214.00.00",
    @Json(name = "androidSdkVersion") val androidSdkVersion: Int? = null,
    @Json(name = "hl") val hl: String = "en",
    @Json(name = "gl") val gl: String = "US"
)

// Simplified representation for parsing player responses
@JsonClass(generateAdapter = true)
data class InnertubePlayerResponse(
    @Json(name = "videoDetails") val videoDetails: InnertubeVideoDetails? = null,
    @Json(name = "streamingData") val streamingData: InnertubeStreamingData? = null
)

@JsonClass(generateAdapter = true)
data class InnertubeVideoDetails(
    @Json(name = "videoId") val videoId: String? = null,
    @Json(name = "title") val title: String? = null,
    @Json(name = "author") val author: String? = null,
    @Json(name = "lengthSeconds") val lengthSeconds: String? = null,
    @Json(name = "thumbnail") val thumbnail: InnertubeThumbnailGroup? = null
)

@JsonClass(generateAdapter = true)
data class InnertubeThumbnailGroup(
    @Json(name = "thumbnails") val thumbnails: List<InnertubeThumbnail>? = null
)

@JsonClass(generateAdapter = true)
data class InnertubeThumbnail(
    @Json(name = "url") val url: String? = null,
    @Json(name = "width") val width: Int? = null,
    @Json(name = "height") val height: Int? = null
)

@JsonClass(generateAdapter = true)
data class InnertubeStreamingData(
    @Json(name = "expiresInSeconds") val expiresInSeconds: String? = null,
    @Json(name = "formats") val formats: List<InnertubeFormat>? = null,
    @Json(name = "adaptiveFormats") val adaptiveFormats: List<InnertubeFormat>? = null
)

@JsonClass(generateAdapter = true)
data class InnertubeFormat(
    @Json(name = "itag") val itag: Int? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "mimeType") val mimeType: String? = null,
    @Json(name = "bitrate") val bitrate: Long? = null,
    @Json(name = "contentLength") val contentLength: String? = null,
    @Json(name = "audioQuality") val audioQuality: String? = null,
    @Json(name = "qualityHeader") val qualityHeader: String? = null
)
