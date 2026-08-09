from fastapi import APIRouter, HTTPException
from app.models import ArtistDetail
from app.ytmusic_client import ytmusic_client

router = APIRouter()

@router.get("/artists/{artist_id}", response_model=ArtistDetail)
def get_artist(artist_id: str):
    artist = ytmusic_client.get_artist(artist_id)
    if not artist:
        raise HTTPException(status_code=404, detail="Artist not found")
    return artist
