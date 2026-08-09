import logging
import time
from typing import List, Dict, Any, Optional
from ytmusicapi import YTMusic
from app.models import (
    SearchResultItem, SearchResponse, SongDetail,
    ArtistDetail, AlbumDetail, PlaylistDetail, LyricsResponse
)

logger = logging.getLogger("ytmusic_client")

class YTMusicClient:
    def __init__(self):
        try:
            self.yt = YTMusic()
            logger.info("YTMusic client initialized successfully")
        except Exception as e:
            logger.error(f"Failed to initialize YTMusic: {e}")
            self.yt = None

    def _extract_thumbnail(self, thumbnails: Any) -> str:
        if not thumbnails or not isinstance(thumbnails, list):
            return ""
        # Get highest resolution thumbnail
        return thumbnails[-1].get("url", "") if thumbnails else ""

    def _extract_artists(self, artists_data: Any) -> str:
        if not artists_data or not isinstance(artists_data, list):
            return "Unknown Artist"
        names = [a.get("name", "") for a in artists_data if isinstance(a, dict) and a.get("name")]
        return ", ".join(names) if names else "Unknown Artist"

    def search(self, query: str) -> SearchResponse:
        if not query.strip() or not self.yt:
            return SearchResponse()

        try:
            results = self.yt.search(query)
            songs: List[SearchResultItem] = []
            artists: List[SearchResultItem] = []
            albums: List[SearchResultItem] = []
            playlists: List[SearchResultItem] = []
            all_items: List[SearchResultItem] = []

            for item in results:
                category = item.get("resultType", "song")
                item_id = item.get("videoId") or item.get("browseId") or item.get("playlistId") or ""
                if not item_id:
                    continue

                title = item.get("title", "Untitled")
                artist = self._extract_artists(item.get("artists", []))
                album = item.get("album", {}).get("name", "") if isinstance(item.get("album"), dict) else ""
                thumbnail = self._extract_thumbnail(item.get("thumbnails"))
                duration = item.get("duration", "")

                res_item = SearchResultItem(
                    id=item_id,
                    title=title,
                    artist=artist,
                    album=album,
                    thumbnail=thumbnail,
                    duration=duration,
                    type=category,
                    provider="ytmusic",
                    playbackAvailable=False,
                    streamUrl=None
                )

                all_items.append(res_item)
                if category == "song":
                    songs.append(res_item)
                elif category == "artist":
                    artists.append(res_item)
                elif category == "album":
                    albums.append(res_item)
                elif category == "playlist":
                    playlists.append(res_item)

            return SearchResponse(
                results=all_items,
                songs=songs,
                artists=artists,
                albums=albums,
                playlists=playlists
            )
        except Exception as e:
            logger.error(f"Search failed for query '{query}': {e}")
            raise e

    def get_song(self, video_id: str) -> Optional[SongDetail]:
        if not self.yt:
            return None
        try:
            watch_playlist = self.yt.get_watch_playlist(videoId=video_id)
            tracks = watch_playlist.get("tracks", [])
            if not tracks:
                return None
            track = tracks[0]
            title = track.get("title", "Unknown Title")
            artist = self._extract_artists(track.get("artists", []))
            artists_list = [a.get("name") for a in track.get("artists", []) if isinstance(a, dict) and a.get("name")]
            album = track.get("album", {}).get("name", "") if isinstance(track.get("album"), dict) else ""
            thumbnail = self._extract_thumbnail(track.get("length")) or self._extract_thumbnail(track.get("thumbnail"))

            return SongDetail(
                id=video_id,
                title=title,
                artist=artist,
                artists=artists_list,
                album=album,
                thumbnail=thumbnail,
                duration=track.get("length", ""),
                provider="ytmusic",
                playbackAvailable=False,
                streamUrl=None
            )
        except Exception as e:
            logger.error(f"get_song failed for '{video_id}': {e}")
            return None

    def get_artist(self, artist_id: str) -> Optional[ArtistDetail]:
        if not self.yt:
            return None
        try:
            artist_data = self.yt.get_artist(channelId=artist_id)
            name = artist_data.get("name", "Unknown Artist")
            thumbnail = self._extract_thumbnail(artist_data.get("thumbnails"))
            description = artist_data.get("description", "")

            songs: List[SearchResultItem] = []
            albums: List[SearchResultItem] = []

            # Extract popular songs if present
            songs_section = artist_data.get("songs", {}).get("results", [])
            for s in songs_section:
                songs.append(SearchResultItem(
                    id=s.get("videoId", ""),
                    title=s.get("title", "Untitled"),
                    artist=name,
                    album=s.get("album", {}).get("name", "") if isinstance(s.get("album"), dict) else "",
                    thumbnail=self._extract_thumbnail(s.get("thumbnails")),
                    duration="",
                    type="song",
                    provider="ytmusic"
                ))

            # Extract albums
            albums_section = artist_data.get("albums", {}).get("results", [])
            for a in albums_section:
                albums.append(SearchResultItem(
                    id=a.get("browseId", ""),
                    title=a.get("title", "Untitled"),
                    artist=name,
                    thumbnail=self._extract_thumbnail(a.get("thumbnails")),
                    type="album",
                    provider="ytmusic"
                ))

            return ArtistDetail(
                id=artist_id,
                name=name,
                thumbnail=thumbnail,
                description=description,
                songs=songs,
                albums=albums
            )
        except Exception as e:
            logger.error(f"get_artist failed for '{artist_id}': {e}")
            return None

    def get_album(self, album_id: str) -> Optional[AlbumDetail]:
        if not self.yt:
            return None
        try:
            album_data = self.yt.get_album(browseId=album_id)
            title = album_data.get("title", "Untitled Album")
            artist = self._extract_artists(album_data.get("artists", []))
            thumbnail = self._extract_thumbnail(album_data.get("thumbnails"))
            year = str(album_data.get("year", ""))

            tracks: List[SearchResultItem] = []
            for t in album_data.get("tracks", []):
                tracks.append(SearchResultItem(
                    id=t.get("videoId", ""),
                    title=t.get("title", "Untitled Track"),
                    artist=self._extract_artists(t.get("artists", [])) or artist,
                    album=title,
                    thumbnail=thumbnail,
                    duration=t.get("duration", ""),
                    type="song",
                    provider="ytmusic"
                ))

            return AlbumDetail(
                id=album_id,
                title=title,
                artist=artist,
                thumbnail=thumbnail,
                year=year,
                tracks=tracks
            )
        except Exception as e:
            logger.error(f"get_album failed for '{album_id}': {e}")
            return None

    def get_playlist(self, playlist_id: str) -> Optional[PlaylistDetail]:
        if not self.yt:
            return None
        try:
            pl = self.yt.get_playlist(playlistId=playlist_id)
            title = pl.get("title", "Untitled Playlist")
            description = pl.get("description", "")
            thumbnail = self._extract_thumbnail(pl.get("thumbnails"))
            author = pl.get("author", {}).get("name", "") if isinstance(pl.get("author"), dict) else ""

            tracks: List[SearchResultItem] = []
            for t in pl.get("tracks", []):
                if not t.get("videoId"):
                    continue
                tracks.append(SearchResultItem(
                    id=t.get("videoId"),
                    title=t.get("title", "Untitled Track"),
                    artist=self._extract_artists(t.get("artists", [])),
                    album=t.get("album", {}).get("name", "") if isinstance(t.get("album"), dict) else "",
                    thumbnail=self._extract_thumbnail(t.get("thumbnails")),
                    duration=t.get("duration", ""),
                    type="song",
                    provider="ytmusic"
                ))

            return PlaylistDetail(
                id=playlist_id,
                title=title,
                description=description,
                thumbnail=thumbnail,
                author=author,
                tracks=tracks
            )
        except Exception as e:
            logger.error(f"get_playlist failed for '{playlist_id}': {e}")
            return None

    def get_charts(self, country: str = "IN") -> List[SearchResultItem]:
        if not self.yt:
            return []
        try:
            charts = self.yt.get_charts(country=country)
            songs_data = charts.get("videos", {}).get("results", []) or charts.get("trending", {}).get("results", [])
            items: List[SearchResultItem] = []
            for item in songs_data:
                video_id = item.get("videoId")
                if not video_id:
                    continue
                items.append(SearchResultItem(
                    id=video_id,
                    title=item.get("title", "Untitled"),
                    artist=self._extract_artists(item.get("artists", [])),
                    thumbnail=self._extract_thumbnail(item.get("thumbnails")),
                    type="song",
                    provider="ytmusic"
                ))
            return items
        except Exception as e:
            logger.error(f"get_charts failed for country '{country}': {e}")
            return []

    def get_lyrics(self, video_id: str) -> LyricsResponse:
        if not self.yt:
            return LyricsResponse(lyrics=None, available=False)
        try:
            watch = self.yt.get_watch_playlist(videoId=video_id)
            lyrics_id = watch.get("lyrics")
            if not lyrics_id:
                return LyricsResponse(lyrics=None, available=False)
            lyrics_data = self.yt.get_lyrics(browseId=lyrics_id)
            lyrics_text = lyrics_data.get("lyrics", "")
            if lyrics_text:
                return LyricsResponse(lyrics=lyrics_text, available=True)
            return LyricsResponse(lyrics=None, available=False)
        except Exception as e:
            logger.error(f"get_lyrics failed for '{video_id}': {e}")
            return LyricsResponse(lyrics=None, available=False)

    def health_check(self) -> Dict[str, Any]:
        if not self.yt:
            return {"status": "error", "ytmusicapi": "disconnected", "message": "YTMusic client not initialized"}
        try:
            self.yt.search("test", limit=1)
            return {"status": "ok", "ytmusicapi": "connected", "service": "Aurora Music Backend", "version": "1.0.0"}
        except Exception as e:
            return {"status": "degraded", "ytmusicapi": "error", "message": str(e)}

ytmusic_client = YTMusicClient()
