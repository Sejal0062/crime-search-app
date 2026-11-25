# CrimeWatch — India-wide news feed update

## What changed
- Dashboard automatically requests crime reports across India without requiring a keyword.
- Search remains available for specific crimes; optional city/state narrows matching reports.
- Live NewsAPI results are sorted by publication time, deduplicated and limited to 24 relevant articles.
- Crime-related YouTube videos appear once in a dedicated section rather than repeating under every article.
- Improved responsive layout for video cards and search controls.
- Browsing and searching remain public; login and registration are optional.

## Run
1. Keep your existing `.env` file in the project root; do not share it. It should define `DB_PASSWORD`, `NEWS_API_KEY`, and `VIDEO_API_KEY`.
2. From the project root in PowerShell run `docker compose up --build -d`.
3. Visit http://localhost:8081/dashboard.

## Notes
- NewsAPI and YouTube quotas, provider indexing, and available article text affect coverage. The feed cannot guarantee *every* crime report from every Indian publisher.
- The location filter checks the city/state against the article headline and description. It may exclude relevant stories that omit the location there.
- Your existing Docker and PostgreSQL settings were preserved; no database reset is required.
- API keys are not included in this ZIP.
