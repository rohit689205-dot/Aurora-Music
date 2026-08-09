from typing import List
from fastapi import APIRouter, Query
from app.models import SearchResultItem
from app.ytmusic_client import ytmusic_client

router = APIRouter()

@router.get("/charts", response_model=List[SearchResultItem])
def get_charts(country: str = Query("IN", description="2-letter country code, default IN")):
    return ytmusic_client.get_charts(country=country)
