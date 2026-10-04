```md
# Domain Registration Live

A near real-time domain registration dashboard consisting of three services:

- **Backend** — Spring Boot / Java
- **Splitter Service** — Python microservice
- **Frontend** — Next.js

## Project Structure

```text
DomainRegistrationLiveDashboard/
│
├── DomainRegistrationLiveBackend/
├── SplitterService/
└── DomainRegistrationLiveFrontend/
```

## Running the Project

All three services need to be running simultaneously.

### 1. Backend

Open a terminal and navigate to the backend directory:

```bash
cd DomainRegistrationLiveBackend
```

Build the Spring Boot project:

```bash
./mvnw clean install
```

Run the application:

```bash
./mvnw spring-boot:run
```

---

### 2. Splitter Service

Open a **new terminal** and navigate to the splitter service:

```bash
cd SplitterService
```

Create a Python virtual environment:

```bash
python3 -m venv venv
```

Activate the virtual environment:

```bash
source venv/bin/activate
```

Install the required dependencies:

```bash
pip install -r requirements.txt
```

Run the Python microservice:

```bash
python3 app.py
```

---

### 3. Frontend

Open another **new terminal** and navigate to the frontend directory:

```bash
cd DomainRegistrationLiveFrontend
```

Install the dependencies:

```bash
npm install
```

Start the Next.js development server:

```bash
npm run dev
```

The terminal will display the URL where the frontend is running.

## Quick Start

After cloning the project, run the following in **three separate terminals**.

### Terminal 1 — Backend

```bash
cd DomainRegistrationLiveBackend
./mvnw clean install
./mvnw spring-boot:run
```

### Terminal 2 — Splitter Service

```bash
cd SplitterService
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python3 app.py
```

### Terminal 3 — Frontend

```bash
cd DomainRegistrationLiveFrontend
npm install
npm run dev
```

Once all three services are running, open the frontend URL shown by the Next.js development server.
```