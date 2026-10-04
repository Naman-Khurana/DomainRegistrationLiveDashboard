# Domain Registration Live

A near real-time dashboard of newly registered domains: a live feed, rolling statistics (top keywords, TLDs, registrars, rising keywords, daily movers) and more, refreshed every few seconds.


| Service  | Stack              | Default port | Role                                              |
| -------- | ------------------ | ------------ | ------------------------------------------------- |
| Backend  | Spring Boot (Java) | 8080         | Stores domains, builds snapshots, serves the live API |
| Splitter | Python             | 8000         | Splits domain names into keywords                 |
| Frontend | Next.js            | 3000         | The dashboard                                     |

## Project structure

```text
DomainRegistrationLiveDashboard/
├── DomainRegistrationLiveBackend/    # Spring Boot application
├── SplitterService/                  # Python microservice
└── DomainRegistrationLiveFrontend/   # Next.js application
```

## Prerequisites

- **Java 21 or newer**
- **PostgreSQL**, running, with a database created for the backend
- **Python 3** with `venv` and `pip`
- **Node.js 18.18 or newer** with `npm`

## Configuration

Check these once before the first run:

- **Backend database:** set the PostgreSQL URL, username and password in the backend's configuration under `DomainRegistrationLiveBackend/src/main/resources/`.
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

trl+C` in each terminal. In the splitter terminal, run `deactivate` to leave the virtual environment.