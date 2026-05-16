# URL Shortener — DevOps Project

A URL shortener built with Spring Boot, Redis, PostgreSQL, Docker Compose, and GitHub Actions CI.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend API | Spring Boot 3.2 (Java 17) |
| Cache | Redis 7 |
| Database | PostgreSQL 15 |
| Containerization | Docker + Docker Compose |
| CI Pipeline | GitHub Actions |

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/shorten` | Shorten a URL |
| GET | `/{code}` | Redirect to original URL |
| GET | `/stats` | All URLs + click counts |
| GET | `/stats/{code}` | Stats for one short code |
| GET | `/health` | Health check |

## Running the Project

### Prerequisites
- Docker Desktop installed and running

### Start all containers
```bash
docker compose up --build
```

### Test the API

**Shorten a URL:**
```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.google.com"}'
```

**Visit the short URL** (in browser or curl):
```
http://localhost:8080/{shortCode}
```

**View all stats:**
```bash
curl http://localhost:8080/stats
```

**Check Redis cache directly:**
```bash
docker exec -it url-shortener-redis redis-cli
GET <your-short-code>
```

### Stop all containers
```bash
docker compose down
```

## GitHub Actions CI Setup

1. Push this project to a GitHub repository
2. Go to Settings → Secrets → Actions → New repository secret
3. Add:
   - `DOCKERHUB_USERNAME` — your Docker Hub username
   - `DOCKERHUB_TOKEN` — Docker Hub access token (Account Settings → Security)
4. Push to `main` — the CI pipeline runs automatically

## Project Structure

```
url-shortener/
├── src/main/java/com/urlshortener/
│   ├── UrlShortenerApplication.java
│   ├── controller/
│   │   ├── UrlController.java
│   │   └── GlobalExceptionHandler.java
│   ├── service/
│   │   └── UrlService.java
│   ├── model/
│   │   └── UrlMapping.java
│   └── repository/
│       └── UrlRepository.java
├── src/main/resources/
│   └── application.properties
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── .github/workflows/ci.yml
```

## How it Works

1. User POSTs a long URL → Spring Boot generates a 7-char code → saved to PostgreSQL → cached in Redis for 24h
2. User visits short URL → Spring Boot checks Redis first (fast) → if miss, queries PostgreSQL → redirects user
3. Click count is incremented in PostgreSQL on every visit
4. GitHub Actions triggers on every push → builds JAR → builds Docker image → pushes to Docker Hub
