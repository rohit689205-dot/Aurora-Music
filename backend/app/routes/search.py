from fastapi import APIRouter, Query, HTTPException
from fastapi.responses import JSONResponse
from app.models import SearchResponse
from app.ytmusic_client import ytmusic_client

router = APIRouter()

@router.get("/search")
def search(q: str = Query(..., min_length=1, description="Search query string")):
    try:
        res = ytmusic_client.search(q)
        return res
    except Exception as e:
        return JSONResponse(
            status_code=500,
            content={
                "error": True,
                "code": "YTMUSIC_ERROR",
                "message": str(e)
            }
        )
