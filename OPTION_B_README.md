# CrimeWatch — Option B upgrade

## Included
- New responsive editorial-style dashboard with a compact hero and news-card grid.
- India-wide feed on initial page load; search keyword and city/state are optional.
- Crime category quick filters, refresh control, source links, and publication-date filtering.
- Separate deduplicated related-video gallery.
- Public access; login/register removed from navigation and React routes (source retained).
- Published date supplied from live NewsAPI articles. Previously cached records have no publication date, so date filters omit them.

## Setup
1. Extract to a new project folder. Copy your own `.env` file into the project root (do not commit it).
2. From PowerShell in the project root run `docker compose up --build -d`.
3. Open `http://localhost:8081/dashboard`.
4. To inspect backend startup, run `docker compose logs --tail=60 crime_backend`.

## Limitations
- Date filtering is performed on the reports returned by the backend (up to 24), not all historical news. A date filter is not a comprehensive archive search.
- The location field is free text; location matching depends on the article title/description and may exclude relevant articles.
- NewsAPI and YouTube API limits and coverage apply. News items are not independently verified.
- No authentication is needed to browse. The old authentication backend code is left in place.
- Existing PostgreSQL data is not migrated or deleted.
