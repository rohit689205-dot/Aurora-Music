from fastapi import APIRouter, Query, HTTPException
from app.models import SearchResponse
from app.ytmusic_client import ytmusic_client

router = APIRouter()

@router.get("/search", response_model=SearchResponse)
def search(q: str = Query(..., min_length=1, description="Search query string")):
    try:
        res = ytmusic_client.search(q)
        return res
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
