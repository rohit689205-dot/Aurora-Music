from fastapi import APIRouter, HTTPException
from app.models import SongDetail, LyricsResponse
from app.ytmusic_client import ytmusic_client

router = APIRouter()

@router.get("/songs/{video_id}", response_model=SongDetail)
def get_song(video_id: str):
    song = ytmusic_client.get_song(video_id)
    if not song:
        raise HTTPException(status_code=404, detail="Song not found")
    return song

@router.get("/songs/{video_id}/lyrics", response_model=LyricsResponse)
def get_lyrics(video_id: str):
    return ytmusic_client.get_lyrics(video_id)
