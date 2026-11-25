# CrimeWatch — Crime Awareness Platform

A React + Spring Boot application for searching crime-related news and public awareness videos, with PostgreSQL storage and Docker Compose support.

## Improvements in this version

- Responsive dark navy/red dashboard and redesigned landing, login and registration pages.
- News results are checked for the requested keyword and crime-related context; common entertainment/review results are filtered.
- Relevant news articles are cached in PostgreSQL and reused if the live news search returns no relevant articles.
- Duplicate article URLs are skipped when caching.
- Login/register use relative `/api/...` URLs so the browser works through the Docker frontend proxy.
- Nginx preserves the `/api` prefix when forwarding requests to Spring Boot.
- Docker Compose no longer calls a missing wait script or an incorrectly named JAR.

## Run with Docker

1. Install and start Docker Desktop.
2. In this project root, create a file named `.env` with your API keys:

   ```dotenv
   NEWS_API_KEY=your_news_api_key
   VIDEO_API_KEY=your_youtube_api_key
   DB_PASSWORD=choose_a_local_database_password
   ```

   Keep `.env` private and do not commit it to Git.

3. Open a terminal in the folder containing `docker-compose.yml` and run:

   ```powershell
   docker compose up --build -d
   ```

4. Open the application at **http://localhost:8081**. The backend is mapped to **http://localhost:8080** and PostgreSQL to port **5432**.

5. View running services and logs:

   ```powershell
   docker compose ps
   docker compose logs -f crime_backend
   docker compose logs -f crime_frontend
   ```

6. Stop the containers:

   ```powershell
   docker compose down
   ```

   To also delete the PostgreSQL volume and all stored database data, use `docker compose down -v`. Do not use `-v` if you want to keep saved articles and users.

## Run locally for development

- Start PostgreSQL and make sure the database `crime_db` exists.
- Configure `DB_PASSWORD`, `NEWS_API_KEY`, and `VIDEO_API_KEY` in your environment.
- Start the Spring Boot backend from `crime-awareness-backend/crime-awareness` with `.\mvnw.cmd spring-boot:run`.
- Start the React frontend from `crime-awareness-frontend` with `npm install` and `npm start`.

The React development server proxies `/api` requests to `http://localhost:8080`; the Docker production frontend proxies them through Nginx to the backend container.

## Notes

- News and video availability depends on third-party API access, API quotas, and the source's current content.
- Search relevance filtering reduces obvious unrelated results but cannot guarantee that every article is a confirmed real-world incident. Always check the original publisher.
- Use a private database password in `.env`; never commit that file. Change any development credentials before using this project in a shared or public environment.
- The login flow is a basic project authentication flow, not a complete production identity system.
