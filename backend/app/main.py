import time
import logging
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from app.routes import search, songs, artists, albums, playlists, charts
from app.models import DiagnosticsStatus

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("aurora_backend")

app = FastAPI(
    title="Aurora Music API",
    description="FastAPI Backend for YouTube Music metadata integration via ytmusicapi",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Diagnostics state
diagnostics_info = DiagnosticsStatus()

@app.middleware("http")
async def log_requests(request: Request, call_next):
    start_time = time.time()
    response = await call_next(request)
    duration = (time.time() - start_time) * 1000
    
    diagnostics_info.lastRequest = str(request.url)
    diagnostics_info.httpStatus = response.status_code
    diagnostics_info.searchLatencyMs = duration
    
    return response

app.include_router(search.router, prefix="/api", tags=["Search"])
app.include_router(songs.router, prefix="/api", tags=["Songs"])
app.include_router(artists.router, prefix="/api", tags=["Artists"])
app.include_router(albums.router, prefix="/api", tags=["Albums"])
app.include_router(playlists.router, prefix="/api", tags=["Playlists"])
app.include_router(charts.router, prefix="/api", tags=["Charts"])

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "Aurora Music Backend", "version": "1.0.0"}

@app.get("/api/diagnostics", response_model=DiagnosticsStatus)
def get_diagnostics():
    return diagnostics_info

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
