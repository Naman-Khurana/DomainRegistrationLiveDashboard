# Domain Registration Live

A near real-time dashboard of newly registered domains: a live feed and rolling statistics (top keywords, TLDs, registrars, rising keywords, daily movers), refreshed every few seconds.

Three services, backed by PostgreSQL and Redis:

| Service  | Stack              | Default port | Role                                                  |
| -------- | ------------------ | ------------ | ----------------------------------------------------- |
| Backend  | Spring Boot (Java) | 8080         | Stores domains, builds snapshots, serves the live API |
| Splitter | Python             | 8000         | Splits domain names into keywords                     |
| Frontend | Next.js            | 3000         | The dashboard                                         |

## Project structure

```text
DomainRegistrationLiveDashboard/
├── DomainRegistrationLiveBackend/    # Spring Boot application
├── SplitterService/                  # Python microservice
└── DomainRegistrationLiveFrontend/   # Next.js application
```

## Prerequisites

- **Java 21 or newer** (the version your `pom.xml` targets)
- **PostgreSQL**, running, with a database created for the backend
- **Redis**, running
- **Python 3** with `venv` and `pip`
- **Node.js 18.18 or newer** with `npm`

## Configuration

Check these once before the first run:

- **Backend database and Redis:** set the PostgreSQL URL, username and password, and the Redis host and port, in the backend's configuration under `DomainRegistrationLiveBackend/src/main/resources/`.
- **Backend → splitter:** the backend must know where the splitter service runs (`splitter.url` in the same configuration).
- **Frontend → backend:** the API addresses used by the dashboard are in `DomainRegistrationLiveFrontend/app/constants/url_constants`.

## Running the project

All three services must be running at the same time. Open **three separate terminals** from the project root.

### Terminal 1: Backend

```bash
cd DomainRegistrationLiveBackend
./mvnw clean install
./mvnw spring-boot:run
```

### Terminal 2: Splitter service

```bash
cd SplitterService
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python3 app.py
```

Next time, only `source venv/bin/activate` and `python3 app.py` are needed.

### Terminal 3: Frontend

```bash
cd DomainRegistrationLiveFrontend
npm install
npm run dev
```

Open the URL printed by the Next.js dev server (usually <http://localhost:3000>).

> **Windows:** use `mvnw.cmd` instead of `./mvnw`, `venv\Scripts\activate` instead of `source venv/bin/activate`, and `python` instead of `python3`.

## Architecture

### Flow

1. **Ingest:** domains arrive in batches and are stored with status `PENDING`.
2. **Split:** a background worker takes batches of 100 `PENDING` domains and sends each batch to the Python splitter.
3. **Store:** the splitter returns the keywords for each domain. The backend saves them and sets the domains to `PARSED`.
4. **Snapshot:** separately, every 8 seconds a background job builds one snapshot of all statistics from the `PARSED` domains, stores it in PostgreSQL and caches it in Redis.
5. **Serve:** the frontend loads the initial feed once, then polls about every 8 seconds. The API returns the latest precomputed snapshot; its `feed` holds the newly registered domains, which the frontend merges into the live list (duplicates skipped).

### Design decisions

- **`PENDING` / `PARSED` status decouples ingestion from splitting.** Ingestion only stores domains, so a slow or stopped splitter never blocks it; unsplit domains wait as `PENDING`.
- **Splitting in batches of 100** means one request to the splitter per batch instead of one per domain.
- **Only `PARSED` domains are counted,** so every statistic is built from complete data (domain and keywords).
- **Statistics are precomputed in the background, not per request.** Database load stays the same however many people are watching.
- **PostgreSQL stores each snapshot; Redis caches it** for quick reads.
- **The frontend polls instead of keeping a connection open.** A snapshot only changes every 8 seconds, so polling faster would return the same data.
- **The feed endpoint is called once;** after that the live list grows from each snapshot's `feed`, so the page makes one recurring request.

### Assumptions / Limitations
1. It is assumed that domain registrations are received through the /v1/ingest endpoint.
2. All ingested registrations are assumed to be confirmed and valid.
3. Registrar ID-to-name mapping has not been implemented; registrar names are represented as ICANN #<ID>.
4. The implementation is an associated implementation of the dotweekly.com/keywords/live page, with some features simplified or omitted due to time constraints.