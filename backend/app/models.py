from typing import List, Optional
from pydantic import BaseModel

class SearchResultItem(BaseModel):
    id: str
    title: str
    artist: str
    album: Optional[str] = ""
    thumbnail: Optional[str] = ""
    duration: Optional[str] = ""
    type: str = "song"
    provider: str = "ytmusic"
    playbackAvailable: bool = False
    streamUrl: Optional[str] = None

class SearchResponse(BaseModel):
    results: List[SearchResultItem] = []
    songs: List[SearchResultItem] = []
    artists: List[SearchResultItem] = []
    albums: List[SearchResultItem] = []
    playlists: List[SearchResultItem] = []

class SongDetail(BaseModel):
    id: str
    title: str
    artist: str
    artists: List[str] = []
    album: Optional[str] = ""
    thumbnail: Optional[str] = ""
    duration: Optional[str] = ""
    provider: str = "ytmusic"
    playbackAvailable: bool = False
    streamUrl: Optional[str] = None

class ArtistDetail(BaseModel):
    id: str
    name: str
    thumbnail: Optional[str] = ""
    description: Optional[str] = ""
    songs: List[SearchResultItem] = []
    albums: List[SearchResultItem] = []

class AlbumDetail(BaseModel):
    id: str
    title: str
    artist: str
    thumbnail: Optional[str] = ""
    year: Optional[str] = ""
    tracks: List[SearchResultItem] = []

class PlaylistDetail(BaseModel):
    id: str
    title: str
    description: Optional[str] = ""
    thumbnail: Optional[str] = ""
    author: Optional[str] = ""
    tracks: List[SearchResultItem] = []

class LyricsResponse(BaseModel):
    lyrics: Optional[str] = None
    available: bool = False

class DiagnosticsStatus(BaseModel):
    backendStatus: str = "online"
    lastRequest: str = "N/A"
    httpStatus: int = 200
    resultCount: int = 0
    searchLatencyMs: float = 0.0
    lastError: Optional[str] = None
    currentProvider: str = "ytmusicapi"
