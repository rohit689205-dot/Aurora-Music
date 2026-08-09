from fastapi import APIRouter, HTTPException
from app.models import PlaylistDetail
from app.ytmusic_client import ytmusic_client

router = APIRouter()

@router.get("/playlists/{playlist_id}", response_model=PlaylistDetail)
def get_playlist(playlist_id: str):
    playlist = ytmusic_client.get_playlist(playlist_id)
    if not playlist:
        raise HTTPException(status_code=404, detail="Playlist not found")
    return playlist
