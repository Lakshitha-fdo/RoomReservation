# CIS6003 Online Room Reservation System Report

## 1. Introduction

### 1.1 Background

Hotels of all sizes depend on reliable reservation systems to manage guest bookings, room allocation, check-in/check-out schedules, and billing. Manual reservation handling creates several practical problems: duplicated bookings, incorrect dates, lost customer information, delayed billing, and poor service speed during front-desk operations. A digital reservation system improves consistency by enforcing structure, validating user input, and storing records in a persistent database.

The project developed for this assignment is an Online Room Reservation System for a fictional hotel, Ocean View Resort. The implementation follows the assignment requirement to use Java without enterprise or rapid-development frameworks. Therefore, the system is intentionally built from first principles using core Java technologies, JDBC, Swing, and Java's built-in HTTP server.

### 1.2 Problem Statement

Ocean View Resort needs a reservation system that supports basic but critical staff operations:
- Authenticate staff users.
- Add reservation records with guest details and stay dates.
- Retrieve reservation details by reservation number.
- Compute billing totals based on room type and number of nights.
- Provide on-screen operational help.
- Allow safe and clean program exit.

The system must also satisfy an academic requirement for distributed architecture through web services, meaning the user interface should communicate with a separate service layer over HTTP rather than direct database access.

### 1.3 Objectives

The primary objectives are:
1. Build a working Java desktop application that satisfies all required features.
2. Implement persistent storage with JDBC and a relational schema.
3. Expose core functions via REST-like API endpoints using only built-in Java libraries.
4. Apply software design patterns (MVC, DAO, Singleton, Factory).
5. Produce automated tests demonstrating correctness and TDD-style thinking.
6. Document UML design, development choices, testing process, and Git workflow.

### 1.4 Scope

The delivered scope includes:
- Desktop staff client (Swing)
- HTTP API server (`com.sun.net.httpserver.HttpServer`)
- SQLite database schema and initialization
- Validation and business logic services
- Test suite (unit + integration)
- UML diagrams and technical documentation

Out-of-scope items include online payment gateways, multi-branch property management, room inventory conflict optimization, customer self-service portal, and production security hardening.

### 1.5 Technology Constraints

To comply with the assignment constraint "no frameworks," the implementation explicitly avoids Spring, Hibernate, Jakarta EE server frameworks, and third-party REST frameworks. The stack is:
- Java 17+ language features (runs on Java 21 in development environment)
- Swing for desktop GUI
- JDBC for database access
- SQLite for local relational storage
- `HttpServer` for API hosting
- `HttpURLConnection` for API consumption
- JUnit 5 for testing

This constraint encouraged a deeper understanding of core Java APIs and architecture fundamentals.

## 2. System Design

### 2.1 Architectural Style

The system follows a layered architecture with clear separation of concerns:
- Presentation/UI Layer: Swing views and controllers
- Client Communication Layer: HTTP client wrapper
- API Layer: request parsing, endpoint routing, response formatting
- Service Layer: business validation and billing logic
- Data Access Layer: DAO contracts and JDBC implementations
- Data Layer: SQLite schema

This design improves maintainability because each layer has a single responsibility and can be tested in isolation.

### 2.2 Distributed Design Rationale

Although this is a desktop application, the assignment requested distributed application behavior. To meet this requirement, the Swing app never calls DAO classes directly. Instead, all operations are performed through HTTP endpoints. This design simulates real-world client-server systems and demonstrates service boundaries clearly.

### 2.3 UML Overview

Three key UML diagram types are included in `docs/uml`:
- Use Case Diagram: captures actor interactions with system functions.
- Class Diagram: shows domain entities, service classes, DAO contracts, and UI/controller classes.
- Sequence Diagrams: show runtime flow for Add Reservation and Generate Bill scenarios.

These diagrams were written as PlantUML source so they are versionable and easy to update as design evolves.

### 2.4 Data Model

Two database tables are used:
1. `users`
   - `username` (PK)
   - `password`
2. `reservations`
   - `reservation_id` (PK)
   - `guest_name`
   - `address`
   - `contact_number`
   - `room_type`
   - `checkin_date`
   - `checkout_date`

A default user (`admin` / `1234`) is seeded for demonstration and testing.

### 2.5 Functional Flow Design

Core flows:
- Login: credentials -> API -> AuthService -> UserDao -> DB
- Add Reservation: form input -> API -> ReservationService -> validation -> DAO insert
- View Reservation: reservation ID -> API -> DAO lookup -> JSON response
- Calculate Bill: reservation ID -> BillingService -> nights/rate/total -> JSON response

Each flow returns explicit success/failure responses with user-readable messages.

## 3. System Development

### 3.1 Project Setup

A Maven project was created with Maven Wrapper so the build does not depend on local Maven installation. Dependencies are minimal: SQLite JDBC and JUnit 5 only. The source structure follows standard Maven conventions.

### 3.2 Domain Models

Implemented domain objects:
- `Reservation`: reservation number, guest data, room type, check-in/out dates.
- `Bill`: derived billing information (nights, rate, total).
- `User`: login credentials model.
- `RoomType`: enum with fixed room rates (`STANDARD`, `DELUXE`, `SUITE`).
- `ServiceResult<T>`: generic result wrapper for service outcomes.

The model layer is intentionally framework-free and simple.

### 3.3 Validation and Utility Logic

Utility classes centralize rules:
- `ValidationUtil`: checks mandatory fields, numeric contact, and date sequence.
- `DateUtil`: computes nights and enforces checkout-after-checkin rule.
- `BillCalculator`: computes total from nights and rate.
- `RoomRateFactory`: maps room types to pricing strategy.

This modularization keeps business rules reusable across API and tests.

### 3.4 Database and DAO Implementation

`DatabaseConnectionManager` implements Singleton behavior for DB configuration and schema initialization. On first creation, it executes `schema.sql`, creating required tables and seeding a default user.

DAO contracts:
- `UserDao`
- `ReservationDao`

JDBC implementations:
- `JdbcUserDao`
- `JdbcReservationDao`

DAO pattern provides clean abstraction between business logic and SQL statements. SQL remains explicit and easy to audit.

### 3.5 Service Layer

Service classes coordinate validation and persistence:
- `AuthService`: validates input and credentials.
- `ReservationService`: validates reservation, checks uniqueness, stores/fetches data.
- `BillingService`: fetches reservation, computes nights/rate/total, returns `Bill`.

This layer prevents business rules from leaking into API handlers or UI forms.

### 3.6 Web Service Layer

`ApiServer` hosts endpoints using Java `HttpServer`:
- `POST /api/login`
- `POST /api/reservations`
- `GET /api/reservations/{id}`
- `GET /api/bill/{id}`

Request/response JSON is handled using a lightweight custom `SimpleJson` utility. Because external JSON frameworks were avoided, the parser is intentionally minimal and scoped to assignment payloads.

HTTP status code policy:
- 200 for successful retrieval/auth
- 201 for successful reservation creation
- 400 for validation/payload errors
- 401 for invalid login
- 404 for missing resources
- 405 for invalid method

### 3.7 Desktop UI Implementation

The desktop interface uses Swing with MVC-style separation:
- Views: login, main menu, add/view reservation dialogs, billing dialog
- Controllers: login, reservation, billing controllers
- Client: `ApiClient` using `HttpURLConnection`

Flow in operation:
1. User logs in.
2. Main menu offers Add/View/Bill/Help/Exit.
3. Each action triggers HTTP requests to API server.
4. Results are shown in user-friendly dialogs.

The UI includes validation-friendly messaging and protects against malformed date inputs.

### 3.8 Run Modes

The project supports:
- Server-only mode (`ServerMain`)
- Client-only mode (`ClientMain`)
- Combined demo mode (`ApplicationMain`)

This flexibility supports assignment demonstrations and testing convenience.

## 4. Design Patterns

### 4.1 MVC Pattern

MVC is used in the desktop layer:
- Model: business entities (`Reservation`, `Bill`, etc.)
- View: Swing windows/dialogs (`LoginView`, `MainMenuView`, etc.)
- Controller: action mediators (`LoginController`, `ReservationController`, `BillingController`)

Benefits:
- UI event logic separated from presentation code.
- Easier testability for non-UI logic.
- Cleaner maintenance when changing UI components.

### 4.2 DAO Pattern

DAO isolates SQL from service code via interfaces:
- `ReservationService` depends on `ReservationDao`, not concrete JDBC code.
- Implementation can be switched or mocked in future.

Benefits:
- Reduced coupling.
- Better code organization.
- Cleaner unit/integration test boundaries.

### 4.3 Singleton Pattern

`DatabaseConnectionManager` is Singleton to ensure centralized DB configuration and schema setup path. This avoids inconsistent DB initialization and keeps connection configuration in one place.

Benefits:
- Single source of truth for DB URL.
- Automatic schema initialization once.
- Straightforward lifecycle for this assignment-scale application.

### 4.4 Factory Pattern

`RoomRateFactory` abstracts room type to rate mapping. Though simple, this adds extensibility for future pricing rules (seasonal pricing, promotions, weekend multipliers).

Benefits:
- Explicit pricing logic location.
- Better maintainability when pricing rules evolve.

## 5. Database Design

### 5.1 Schema Design Principles

The schema aims for simplicity and correctness:
- Primary keys enforce unique user and reservation IDs.
- Required fields are `NOT NULL`.
- Room type is stored as text enum name for readability.
- Dates are stored as ISO text values compatible with Java `LocalDate`.

### 5.2 Initialization Strategy

`schema.sql` is loaded from classpath on application startup through `DatabaseConnectionManager`. This ensures new environments are self-initializing with no manual SQL step.

### 5.3 CRUD Scope

Current operations implemented:
- Users: credential validation (read)
- Reservations: create and read by ID

This matches assignment requirements. Update/delete operations were intentionally not added to keep scope focused.

### 5.4 Data Integrity Considerations

Integrity protections include:
- Duplicate reservation ID prevention in service logic plus DB primary key.
- Mandatory value checks before insert.
- Date-sequence validation prior to persistence.

Potential future improvements include DB-level `CHECK` constraints for date ordering and stricter phone format rules.

## 6. Testing and Quality Assurance

### 6.1 Testing Approach

A hybrid test approach was used:
- Unit tests for pure computation and validation.
- DAO integration tests with temporary SQLite file.
- API integration test starting actual HTTP server instance.

This gives confidence at algorithm, persistence, and end-to-end API levels.

### 6.2 Unit Tests

Implemented tests:
- `DateUtilTest`: verifies night calculation and invalid date handling.
- `BillCalculatorTest`: verifies total calculation and invalid nights.
- `ValidationUtilTest`: verifies both valid and invalid reservation validation paths.

These tests protect core business rules from regression.

### 6.3 DAO Integration Test

`ReservationDaoTest` uses a temporary DB and validates:
- Insert operation success
- Retrieval by reservation ID
- Field correctness after read

This confirms schema, JDBC mappings, and DAO behavior.

### 6.4 API Integration Test

`ApiServerIntegrationTest` performs a full workflow:
1. Start server on random free port.
2. Login with seeded user.
3. Add reservation via POST endpoint.
4. Retrieve reservation via GET endpoint.
5. Generate bill and verify nights/total.

This is strong evidence that distributed communication and business logic are functioning together.

### 6.5 Manual Testing

Manual UI testing verified:
- Login success/failure messaging
- Reservation form input flow
- Display formatting for reservation detail output
- Bill display values aligned with business rules
- Help dialog visibility
- Safe exit behavior

### 6.6 Defects and Fixes During Development

Typical issues encountered and resolved:
- Build tool unavailability: solved by adding Maven Wrapper.
- Input/date format problems: handled by UI parse guard and service-level validation.
- Duplicate ID behavior: explicit pre-check and DB PK protection.
- API error consistency: standardized JSON fields `success` and `message`.

### 6.7 Residual Risks

Known limitations for assignment scope:
- Minimal JSON parser is intentionally limited.
- Plaintext credentials are not production-safe.
- No concurrency handling for room inventory overlaps.

These are acceptable for educational scope but should be addressed in production evolution.

## 7. GitHub Version Control and Workflow

### 7.1 Repository Strategy

Recommended repository organization:
- Single repository containing source, tests, docs, and UML files.
- Daily commits reflecting incremental progress.
- Optional feature branches for API/UI/testing modules.

### 7.2 Commit Discipline

Planned commit style:
1. Project scaffold and wrapper setup
2. Domain models and utility rules
3. DAO and database layer
4. Services and business logic
5. HTTP API handlers
6. Swing client MVC
7. Automated tests
8. UML and report documents

This progression provides traceability from requirement to implementation.

### 7.3 Benefits of Git Usage in This Project

- Enables rollback and comparison of evolving designs.
- Encourages modular development with clear milestones.
- Supports collaboration and review (if team-based).
- Acts as evidence of continuous development activity for academic assessment.

### 7.4 Suggested Report Evidence

To maximize marks in version-control criteria, include:
- Screenshot of commit history.
- Selected commit messages mapped to development stages.
- Explanation of bug-fix commits.
- Final release tag for submission version.

## 8. Conclusion

The Ocean View Resort Online Room Reservation System was successfully built according to assignment constraints and functional requirements. The application includes login, reservation entry, reservation lookup, billing, help guidance, and safe exit. It uses a proper relational database through JDBC and demonstrates a distributed architecture where the Swing client communicates with HTTP web services.

From a software engineering perspective, the design applies MVC, DAO, Singleton, and Factory patterns in practical ways. Automated tests validate critical business rules and integration paths. UML diagrams and project documentation provide traceable design communication and support formal report requirements.

The project demonstrates that robust software can be developed using only core Java components when architecture and validation are handled carefully. While intentionally scoped for academic evaluation, the resulting codebase forms a strong foundation for future enhancements such as secure authentication, richer room inventory management, and advanced reporting.

## 9. Future Improvements

Although not required for this assignment, future versions can include:
- Password hashing and secure login sessions.
- HTTPS support and API token authentication.
- Reservation update/cancel features.
- Room availability conflict checks.
- PDF invoice export.
- Role-based access control for different staff categories.
- Enhanced JSON parsing and stricter API contracts.

These improvements would move the project from prototype-level academic solution toward production readiness.

## 10. Appendix

### 10.1 API Endpoint Summary

- `POST /api/login` - authenticate staff
- `POST /api/reservations` - create reservation
- `GET /api/reservations/{id}` - fetch reservation details
- `GET /api/bill/{id}` - generate bill for reservation

### 10.2 Room Rate Card

- STANDARD: 2000 per night
- DELUXE: 3500 per night
- SUITE: 4500 per night

### 10.3 Default Demo Credentials

- Username: `admin`
- Password: `1234`

## 11. Detailed Implementation Walkthrough

### 11.1 End-to-End Login Flow

The login flow begins in `LoginView`, where staff enters a username and password. This view does not query the database directly. Instead, it delegates to `LoginController`, which calls `ApiClient.login()`. The client constructs a JSON payload and sends it to `POST /api/login` using `HttpURLConnection`.

On the server side, `ApiServer` routes this request to `LoginHandler`. The handler parses JSON, calls `AuthService.login()`, and receives a `ServiceResult`. `AuthService` performs input checks and calls `UserDao.isValidUser()`. The JDBC implementation runs a parameterized SQL statement against SQLite:

- `SELECT COUNT(1) FROM users WHERE username = ? AND password = ?`

The result is converted into a JSON response with a meaningful HTTP status:
- 200 for valid login
- 401 for invalid login

The client converts the response back into `ClientResult<Void>` and the UI shows success/failure dialogs accordingly.

This flow demonstrates three important quality points:
1. Separation of concerns: UI has no DB logic.
2. Security baseline: prepared statements avoid SQL injection in credential checks.
3. User feedback clarity: API messages are directly consumable in the UI.

### 11.2 Add Reservation Flow

Reservation entry is done through `ReservationFormDialog`. Staff provides:
- reservation number
- guest name
- address
- contact number
- room type
- check-in date
- check-out date

The dialog first parses date strings into `LocalDate`. If parsing fails, the user receives immediate input-format feedback. If parsing succeeds, it builds a `Reservation` model and delegates to `ReservationController`, which calls `ApiClient.addReservation()`.

The API server receives this via `POST /api/reservations`. The handler:
1. Parses JSON.
2. Converts `roomType` into enum using `RoomType.from()`.
3. Converts date strings with `LocalDate.parse`.
4. Calls `ReservationService.addReservation()`.

`ReservationService` performs server-side validation with `ValidationUtil`:
- Required-field checks
- Numeric contact validation
- Date order validation (`checkOut > checkIn`)
- Unique reservation ID check using DAO

If the ID already exists, the service returns:
- `success = false`
- message: `Reservation number already exists.`

If valid and unique, DAO inserts into SQLite:

- `INSERT INTO reservations (...) VALUES (...)`

The API returns 201 Created. The UI displays a success confirmation.

### 11.3 View Reservation Flow

In `ViewReservationDialog`, staff enters reservation number and clicks search. The controller calls `ApiClient.getReservation()`, which sends:

- `GET /api/reservations/{id}`

Server route handling in `ReservationHandler` identifies path-based ID and calls `ReservationService.findById()`. DAO fetches row by PK and maps it into `Reservation` model. Response JSON includes all reservation fields.

If reservation does not exist, API returns 404 with a clear message. The view reacts by displaying an error dialog and clearing prior text, preventing stale information display.

### 11.4 Billing Flow

Billing is intentionally derived (not stored) to avoid duplicated and potentially stale financial values in the database. `BillDialog` sends:

- `GET /api/bill/{id}`

`BillingService.generateBill()` performs:
1. Retrieve reservation from `ReservationService`.
2. Compute nights via `DateUtil.calculateNights()`.
3. Fetch rate via `RoomRateFactory.getNightlyRate()`.
4. Compute total via `BillCalculator.calculateTotal()`.

This decomposition helps test each rule independently. For example, date logic and multiplication logic have independent unit tests, reducing risk of hidden compound errors.

## 12. Validation and Business Rule Matrix

The implementation enforces both client-friendly and server-authoritative validation.

### 12.1 Rule Table

1. Reservation ID must not be blank.
2. Reservation ID must be unique.
3. Guest name must not be blank.
4. Address must not be blank.
5. Contact number must be numeric.
6. Room type must be STANDARD/DELUXE/SUITE.
7. Dates must be valid ISO format.
8. Check-out date must be after check-in date.
9. Username/password must not be blank for login.

### 12.2 Why Server-Side Validation Matters

Although the UI already checks obvious formatting issues, server validation is essential because:
- Different clients could call the API later.
- UI checks can be bypassed by direct HTTP calls.
- Data integrity belongs to business/service layer, not view code.

By validating in services, the system remains robust even if UI changes in future.

### 12.3 Error Messaging Strategy

Error messages are designed for non-technical hotel staff. Examples:
- `Contact number must be numeric.`
- `Check-out date must be after check-in date.`
- `Reservation not found.`
- `Service unreachable. Start API server first.`

This makes the system easier to demonstrate and operate in an assignment presentation.

## 13. API Design Details

### 13.1 Endpoint Contracts

`POST /api/login`  
Input: username, password  
Output: success, message  

`POST /api/reservations`  
Input: reservation object fields  
Output: success, message  

`GET /api/reservations/{id}`  
Output: success, message, reservation fields  

`GET /api/bill/{id}`  
Output: success, message, nights, nightlyRate, total  

### 13.2 HTTP Status Code Mapping

- 200 OK: successful retrieval/auth.
- 201 Created: reservation saved.
- 400 Bad Request: validation or malformed payload.
- 401 Unauthorized: login failure.
- 404 Not Found: reservation or endpoint unavailable.
- 405 Method Not Allowed: wrong HTTP method.

Consistent status code behavior is useful for future API clients and demonstrates strong protocol discipline.

### 13.3 JSON Handling Without Frameworks

To satisfy assignment constraints, no JSON framework was added. A small utility (`SimpleJson`) handles:
- flat object parsing
- string escaping
- primitive serialization (string, number, boolean)

Because payloads are controlled and simple, this approach is acceptable for educational scope. In production, full JSON libraries are safer and richer (nested structures, arrays, schema validation).

## 14. Database and SQL Discussion

### 14.1 Why SQLite Was Selected

SQLite provides important assignment-time benefits:
- Zero server setup
- Portable single-file database
- Repeatable test setup
- Fast local execution

This makes evaluation easier for instructors and avoids environmental dependency issues during demonstration.

### 14.2 SQL Safety and Maintainability

All SQL execution uses prepared statements with placeholders. Benefits:
- Reduces risk of SQL injection
- Cleaner statement readability
- Better separation between SQL structure and data values

SQL is kept in DAO layer only, preventing leakage into UI or service classes.

### 14.3 Schema Initialization Pattern

The singleton DB manager reads `schema.sql` on first start. This creates tables and seeds `admin/1234`. A new machine can run the app without manual SQL setup.

This design also supports tests: a custom DB URL can be provided to initialize isolated temporary databases.

## 15. Testing Strategy in Depth

### 15.1 Why Multiple Test Types Were Used

Single-layer testing is not enough for distributed systems. The project therefore includes:
- Unit tests: deterministic and fast
- Integration tests: realistic and cross-layer

This combined approach catches both algorithmic and wiring failures.

### 15.2 Unit Test Coverage Rationale

`DateUtilTest` ensures duration logic cannot regress. Billing correctness depends entirely on this function.  
`BillCalculatorTest` verifies numeric formula and invalid input behavior.  
`ValidationUtilTest` verifies central business constraints with both valid and invalid scenarios.

These tests are independent from DB and network, giving fast confidence during refactoring.

### 15.3 DAO Integration Test Value

`ReservationDaoTest` validates:
- SQL insert correctness
- read mapping back to model
- compatibility with SQLite type storage

This test was critical because a date-storage mapping issue surfaced and was fixed by storing ISO date strings consistently. This is a good example of tests preventing hidden runtime defects.

### 15.4 API Integration Test Value

`ApiServerIntegrationTest` starts a real server instance on an ephemeral port and exercises:
- login
- reservation create
- reservation retrieval
- bill generation

This verifies protocol flow, serialization/deserialization, service logic, and DAO behavior together.

### 15.5 TDD Reflection

The development process followed test-first/near-test-first rhythm for critical logic:
1. expected billing/date behavior written in tests
2. utility implementations adjusted until pass
3. DAO test used to validate persistence contract
4. API integration test confirmed distributed flow

This produced immediate feedback and reduced rework.

## 16. Git and Version-Control Evidence

### 16.1 Commit Structure Implemented

The codebase was committed in focused increments:
1. `Initialize Maven wrapper and base build configuration`
2. `Add domain models, validation, billing, and JDBC persistence layers`
3. `Implement HTTP API server and Swing client consuming services`
4. `Add JUnit test suite, UML diagrams, and assignment documentation`

This sequence reflects practical software delivery stages and provides clear historical traceability for assessors.

### 16.2 Why Small Commits Matter

Small commits improve:
- review quality (each change has a clear purpose)
- rollback safety (problematic commit can be reverted cleanly)
- progress evidence (daily learning and delivery are visible)

For academic projects, this directly supports grading criteria around development process maturity.

### 16.3 Suggested Future Git Practices

If more time was available, the following would further improve process quality:
- pull-request style branch reviews (even solo, as self-review)
- semantic version tags (`v1.0`, `v1.1`)
- CI automation for `mvn test`
- issue tracker linking commits to requirements/tests

## 17. Limitations and Critical Evaluation

### 17.1 Authentication Simplicity

Credentials are stored in plaintext for assignment simplicity. This is acceptable for demonstration but not secure. Production systems require salted password hashing and secure credential management.

### 17.2 JSON Parser Scope

`SimpleJson` is intentionally minimal and designed for flat payloads. It is not a full JSON standard implementation. A production-grade parser is recommended in real deployments.

### 17.3 Reservation Conflict Logic

The assignment did not require room inventory availability checking. Therefore, overlapping reservations for the same room category are not prevented. A future enhancement should include room stock counts and conflict validation.

### 17.4 UI Modernization

Swing is sufficient and stable, but visual style is basic. JavaFX or web front end could improve user experience in a future iteration.

### 17.5 Security and Transport

HTTP is unencrypted in local use. A real deployment should run HTTPS and token/session-based authentication.

## 18. Lessons Learned

1. Core Java can deliver complete client-server systems when architecture is clean.
2. Even simple apps benefit significantly from layered boundaries.
3. Tests reveal subtle integration issues that visual/manual testing can miss.
4. Explicit error contracts improve both debugging and user experience.
5. Version-control discipline makes final reporting much easier.

The framework-free constraint, while restrictive, provided strong learning value in protocol handling, JDBC mapping, and manual API contract design.

## 19. Final Summary

This project achieved the CIS6003 assignment objectives with a complete, runnable, tested, and documented solution built entirely on core Java technologies. The implementation demonstrates:
- functional completeness (login, reservation CRUD subset, billing, help, exit)
- distributed architecture through HTTP API + desktop client
- proper persistence through JDBC + SQLite
- design-pattern application (MVC, DAO, Singleton, Factory)
- automated verification through JUnit tests
- software engineering documentation with UML and process notes

Overall, the solution is appropriate for assignment assessment and provides a strong base for future extension into a more production-oriented reservation platform.
