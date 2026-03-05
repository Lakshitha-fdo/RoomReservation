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

- STANDARD: 100 per night
- DELUXE: 150 per night
- SUITE: 250 per night

### 10.3 Default Demo Credentials

- Username: `admin`
- Password: `1234`
