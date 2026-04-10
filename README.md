# Real Time Financial Sentiment Dashboard

A full stack real time financial dashboard that analyzes sentiment from financial news headlines using AI, tracks live stock and crypto prices, and displays everything on a live updating dashboard.

** Live Demo: ** [sentiment-dashboard-frontend.vercel.app](https://sentiment-dashboard-frontend.vercel.app)

---

## What It Does

-Automatically fetches financial news headlines every hour for tracked assets
- Uses HuggingFaces's FinBERT AI model to analyze whether news is positive, negative, or neutral
- Displays live sentiment scores and confidence percentages on real time dashboard
- Shows live stock and crypto prices with daily percentage changes
- Tracks active users, page views, and session analytics in real time
- Allows users to add any stock or crypto symbol to a shared watchlist

---

## Architecture

**Data Pipeline:**
1. `DataIngestionService` fetches news headlines every 60 minutes via GNEWS API
2. Headlines are published as Spring Events to decouple ingestion from processing
3. `KafkaConsumerService` picks up each event and calls HuggingFace's finBERT model
4. Sentiment results are saved to postgreSQL via Spring Data JPA
5. WebSocket instantly pushes results to all connected browsers
6. React dashboard updates the live feed and chart in real time

---

##Tech Stack

### Backend
| Technology | Purpose                                               |
|---|-------------------------------------------------------|
|Java 21 + Spring Boot 3 | Backend framework                                     |
| Spring Data JPA + Hibernate | ORM - maps java classes to PostgreSQL tables          |
| Spring WebSocket + STOMP | Real time push to frontend clients                    |
| Spring Scheduler | Automated data collection every 60 minutes            |
| Spring Events | Internal message queue (decouples ingestion from processing) |
| PostgreSQL | Persistent data storage|
| Lombok | Eliminates boilerplate Java code |
| Docker | Containerization for deployment |

### Frontend

| Technology | Purpose |
|---|---|
| React + Vite | Frontend framework |
|Recharts | Live updating sentiment chart |
| STOMP.js + SockJS | WebSocket client |
| Axios | HTTP requests to REST API |

### External APIs

| API | Purpose |
|---|---|
| HuggingFace FinBERT | AI sentiment analysis on financial text |
| GNews API | Financial news headlines |
| Finnhub | Real time stock prices |
| CoinGecko | Real time crypto prices |

### Infrastructure

|Service | Purpose |
|---|---|
| Render | Backend hosting (Docker) |
|Vercel | Frontend hosting |
| GitHub | Version control |

---

##Database Schema

| Table | Description |
|---|---|
| `sentiment_data` | Every analyzed headline with symbol, sentiment label, confidence score and timestamp |
| `stock_price` | Price snapshots for all tracked symbols updated every 60 seconds|
| `user_session` | User visits, session durations, and active status for analytics |
| `watchlist_item | User added symbols with type (stock/crypto) and full name |

---

## Features

### Live Sentiment Analysis
- Analyzes financial news using FinBERT - a BERT model fine tuned specifically on financial text
- Returns sentiment label (POSITIVE/NEGATIVE/NEUTRAL) and confidence score
- Color coded feed: green for positive, red for negative, and yellow for neutral

### Real Time Price Ticker
- Live stock prices via Finnhub API (AAPL, TSLA + watchlist stocks)
- Live crypto prices via CoinGecko API (BTC, ETH + watchlist crypto)
- Daily percentage change with directional arrows
- Horizontally scrollable as more symbols are added

### Dynamic Watchlist
- Users can add any stock or crypto symbol via the + button
- Backend validates symbols against Finnhub (stocks) and CoinGecko (crypto)
- Immediately fetches news and prices for newly added symbols
- Persists to PostgreSQL so symbols are tracked across all sessions

### User Analytics
- Tracks active users in real time via WebSocket heartbeats
- Records page views, session durations and visitor counts
- Broadcasts analytics updates to all connected clients instantly
- Scheduled cleanup detects disconnected users gracefully

### Sentiment Chart
- Live updating line chart showing sentiment scores over time
- Color coded dots: green (positive), amber (neutral), red (negative)
- Neutral reference line at 0.5 for easy visual reference
- Custom tooltip showing symbol, sentiment and confidence on hover

---

## Running Locally

### Prerequisites
- Java 21
- Maven
- PostgreSQL
- Apache Kafka
- Node.js 20+

### Backend Setup

1. Clone the repository:
```bash
git clone https://github.com/Pollak10/sentiment-dashboard.git
cd sentiment-dashboard
```

2. Create `src/main/resources/application-local.properties` with your API keys:
```properties
api.huggingface.key=your_key
api.gnews.key=your_key
api.finnhub.key=your_key
api.coingecko.key=your_key
```

3. Create a PostgreSQL database:
```sql
CREATE DATABASE sentimentdb;
```

4. Start Kafka:
```bash
cd ~/kafka
bin/kafka-server-start.sh config/server.properties
```

5. Run the Spring Boot app:
```bash
./mvnw spring-boot:run
```

### Frontend Setup

1. Clone the frontend repository:
```bash
git clone https://github.com/Pollak10/sentiment-dashboard-frontend.git
cd sentiment-dashboard-frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the dev server:
```bash
npm run dev
```

4. Open [http://localhost:5173](http://localhost:5173)

---

## Deployment

- **Backend:** Deployed on Render using Docker
- **Frontend:** Deployed on Vercel with automatic GitHub deployments
- **Database:** PostgreSQL on Render
- **Keep-alive:** UptimeRobot pings the health endpoint every 5 minutes

---

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/sentiments` | Get all sentiment data |
| GET | `/api/sentiments/{symbol}` | Get sentiment data for a specific symbol |
| GET | `/api/sentiments/prices` | Get latest prices for all tracked symbols |
| GET | `/api/sentiments/analytics` | Get current user analytics |
| GET | `/api/sentiments/watchlist` | Get all watchlist items |
| POST | `/api/sentiments/watchlist/add` | Add a symbol to the watchlist |
| DELETE | `/api/sentiments/watchlist/remove/{symbol}` | Remove a symbol from the watchlist |
| GET | `/api/sentiments/health` | Health check |

---

## Key Engineering Decisions

**Why Spring Events instead of Kafka in production?**
Apache Kafka requires a separate broker process which adds complexity and cost to hosting. For the deployed version I replaced Kafka with Spring's built-in `ApplicationEventPublisher` which provides the same decoupling between ingestion and processing without external dependencies. The interface stayed identical — only the underlying transport changed — demonstrating the value of loose coupling.

**Why FinBERT instead of general sentiment models?**
FinBERT is specifically fine-tuned on financial text (earnings reports, news articles, analyst reports) making it significantly more accurate for financial sentiment analysis than general purpose models like DistilBERT.

**Why WebSocket instead of polling?**
Polling would require the frontend to make HTTP requests every few seconds checking for new data — wasteful and slow. WebSocket maintains a persistent connection so the server can push updates instantly the moment new sentiment data arrives, giving the dashboard a genuinely real-time feel.

---

## Author

Benjamin Pollak — [GitHub](https://github.com/Pollak10)

