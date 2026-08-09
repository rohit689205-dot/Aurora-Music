# Aurora Music FastAPI Backend (ytmusicapi)

This FastAPI backend bridges the Aurora Music Android client with YouTube Music metadata via `ytmusicapi`.

## Setup & Run

```bash
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## Endpoints

- `GET /health` - Health check
- `GET /api/search?q=<query>` - Unified search
- `GET /api/songs/{video_id}` - Song metadata
- `GET /api/artists/{artist_id}` - Artist information & tracks
- `GET /api/albums/{album_id}` - Album tracklist & metadata
- `GET /api/playlists/{playlist_id}` - Playlist tracks & metadata
- `GET /api/charts?country=IN` - Top charts
- `GET /api/songs/{video_id}/lyrics` - Song lyrics
