# Ocean View Resort - Online Room Reservation System (CIS6003)

This project is a Java implementation of an Online Room Reservation System for **Ocean View Resort**.

## Technology Choices

- Core Java **17 / 21 LTS**
- Swing (desktop UI)
- JDBC with SQLite (data storage)
- Built-in `com.sun.net.httpserver.HttpServer` (REST-like web services)
- JUnit 5 (automated testing)

> **Note:** This project is tested with **JDK 17 and JDK 21 (LTS versions)**.  
> Using **JDK 25 or newer may cause Maven test failures** due to compatibility issues with certain testing plugins.
 
## Features

- Login with username and password (`admin` / `1234` seeded)
- Add reservation with validation
- View reservation by reservation number
- Calculate bill (`nights * room rate`)
- Help section
- Safe exit
- Distributed design: Swing client consumes HTTP API via `HttpURLConnection`

## Project Structure

- `src/main/java/com/oceanview/model` - domain models
- `src/main/java/com/oceanview/ui` - Swing MVC controllers/views
- `src/main/java/com/oceanview/client` - HTTP API client
- `src/main/java/com/oceanview/api` - HTTP server and handlers
- `src/main/java/com/oceanview/dao` - DAO interfaces and JDBC implementations
- `src/main/java/com/oceanview/service` - business services
- `src/main/java/com/oceanview/config` - Singleton database connection manager
- `src/main/resources/schema.sql` - SQL schema + seed user
- `src/test/java` - JUnit tests
- `docs/` - UML diagrams and assignment documentation

## Database

SQLite database is created automatically on first run.

Default DB URL:
- `jdbc:sqlite:data/ocean_view.db`

Override DB URL:
- JVM property: `-Ddb.url=jdbc:sqlite:path/to/file.db`

## Build and Test

Use Maven Wrapper (no local Maven required):

```powershell
.\mvnw.cmd test
```

## Run API Server

```powershell
.\mvnw.cmd "-Dexec.mainClass=com.oceanview.api.ServerMain" exec:java
```

Default API port is `8080`.

Override port:

```powershell
.\mvnw.cmd "-Dexec.mainClass=com.oceanview.api.ServerMain" "-Dserver.port=9090" exec:java
```

## Run Desktop Client

Start server first, then in another terminal:

```powershell
.\mvnw.cmd "-Dexec.mainClass=com.oceanview.ui.view.ClientMain" exec:java
```

If server is not on localhost:8080, set base URL:

```powershell
.\mvnw.cmd "-Dexec.mainClass=com.oceanview.ui.view.ClientMain" "-Dapi.baseUrl=http://localhost:9090" exec:java
```

## One-Command Demo (Server + UI)

```powershell
.\mvnw.cmd "-Dexec.mainClass=com.oceanview.ApplicationMain" exec:java
```

## API Endpoints

- `POST /api/login`
- `POST /api/reservations`
- `GET /api/reservations/{id}`
- `GET /api/bill/{id}`

Sample login payload:

```json
{
  "username": "admin",
  "password": "1234"
}
```

## UML

PlantUML source files:
- `docs/uml/use-case.puml`
- `docs/uml/class-diagram.puml`
- `docs/uml/sequence-add-reservation.puml`
- `docs/uml/sequence-generate-bill.puml`

Render with any PlantUML-compatible tool.

## Notes

- Room rates:
  - `STANDARD = 2000`
  - `DELUXE = 3500`
  - `SUITE = 4500`
- Date format in UI/API: `YYYY-MM-DD`
