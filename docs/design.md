# Design Decisions and Assumptions

## 1. Architecture Choice

The implementation uses a layered architecture with clear responsibilities:
- UI layer: Swing views and controllers
- Client layer: HTTP client using `HttpURLConnection`
- API layer: built-in Java `HttpServer`
- Service layer: business rules and orchestration
- DAO layer: JDBC persistence
- Model layer: domain entities

This structure satisfies maintainability requirements and makes each component independently testable.

## 2. Distributed Requirement

To satisfy the distributed application requirement without frameworks:
- The API is exposed over HTTP on a configurable port (default 8080).
- The Swing desktop app does not call DAO or database directly.
- The Swing app communicates only via REST-like endpoints.

This creates a clear client-server split while remaining framework-free.

## 3. Database Technology

SQLite is chosen for local simplicity and reliable demos:
- No external database server installation
- Single file persistence (`data/ocean_view.db`)
- JDBC-compatible
- Easy to isolate in tests with temp DB files

## 4. Core Design Patterns Used

### MVC
- Models: `Reservation`, `Bill`, `User`, `RoomType`
- Views: Swing classes in `ui.view`
- Controllers: classes in `ui.controller`

### DAO
- `UserDao`, `ReservationDao` define persistence contracts
- `JdbcUserDao`, `JdbcReservationDao` provide JDBC implementations

### Singleton
- `DatabaseConnectionManager` ensures a single centralized DB configuration and schema initialization path.

### Factory (optional enhancement)
- `RoomRateFactory` resolves room-type pricing, making pricing logic explicit and central.

## 5. Validation Rules

Validation is handled in service logic and reinforced through API responses:
- Reservation number must be non-empty and unique
- Guest name and address are required
- Contact number must contain digits only
- Room type must be one of STANDARD/DELUXE/SUITE
- Check-out must be after check-in

The UI performs basic formatting checks, while server-side rules are authoritative.

## 6. Billing Logic

- `nights = DAYS.between(checkIn, checkOut)`
- `rate = roomType.nightlyRate`
- `total = nights * rate`

Rates:
- STANDARD = 2000
- DELUXE = 3500
- SUITE = 4500

## 7. Security Assumptions

For assignment scope:
- Credentials are stored in plaintext in SQLite (seed: admin/1234)
- No session tokens or encryption are implemented

In production, this would be replaced by hashed passwords, secure transport (HTTPS), and token/session management.

## 8. Error Handling Strategy

- API returns JSON with `success` and `message` fields consistently.
- HTTP status codes are meaningful:
  - 200 OK, 201 Created
  - 400 Bad Request
  - 401 Unauthorized
  - 404 Not Found
  - 405 Method Not Allowed
- UI displays user-friendly messages via dialogs.

## 9. Assumptions

- Date format accepted as ISO-8601 (`YYYY-MM-DD`)
- Single hotel branch with one reservation table
- No room inventory conflict management is required by brief
- A reservation is identified uniquely by `reservation_id`
- One staff role is sufficient for assignment use case
