import time
import logging
from collections import deque
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from app.routes import search, songs, artists, albums, playlists, charts
from app.models import DiagnosticsStatus, ApiRequestLog
from app.ytmusic_client import ytmusic_client

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
recent_requests = deque(maxlen=5)

@app.middleware("http")
async def log_requests(request: Request, call_next):
    start_time = time.time()
    status_code = 500
    error_msg = None
    try:
        response = await call_next(request)
        status_code = response.status_code
        return response
    except Exception as e:
        error_msg = str(e)
        status_code = 500
        raise e
    finally:
        duration = (time.time() - start_time) * 1000
        url_str = str(request.url)
        
        diagnostics_info.lastRequest = url_str
        diagnostics_info.httpStatus = status_code
        diagnostics_info.searchLatencyMs = duration
        if error_msg:
            diagnostics_info.lastError = error_msg
            
        req_log = ApiRequestLog(
            url=url_str,
            method=request.method,
            httpStatus=status_code,
            latencyMs=duration,
            error=error_msg
        )
        recent_requests.appendleft(req_log)
        diagnostics_info.recentRequests = list(recent_requests)

app.include_router(search.router, prefix="/api", tags=["Search"])
app.include_router(songs.router, prefix="/api", tags=["Songs"])
app.include_router(artists.router, prefix="/api", tags=["Artists"])
app.include_router(albums.router, prefix="/api", tags=["Albums"])
app.include_router(playlists.router, prefix="/api", tags=["Playlists"])
app.include_router(charts.router, prefix="/api", tags=["Charts"])

@app.get("/health")
def health_check():
    return ytmusic_client.health_check()

@app.get("/api/diagnostics", response_model=DiagnosticsStatus)
def get_diagnostics():
    return diagnostics_info

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
