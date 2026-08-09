from fastapi import APIRouter, HTTPException
from app.models import AlbumDetail
from app.ytmusic_client import ytmusic_client

router = APIRouter()

@router.get("/albums/{album_id}", response_model=AlbumDetail)
def get_album(album_id: str):
    album = ytmusic_client.get_album(album_id)
    if not album:
        raise HTTPException(status_code=404, detail="Album not found")
    return album
