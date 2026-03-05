# Test Plan and TDD Notes

## Test Strategy

Testing combines:
- Unit tests for deterministic business logic
- DAO integration tests against isolated SQLite test DB
- API integration test covering request/response flow end-to-end

JUnit 5 is used for all automated tests.

## Test Environment

- Java 17+ runtime
- SQLite JDBC driver
- Temporary filesystem database for integration tests

## Test Data

- Login:
  - Username: `admin`
  - Password: `1234`
- Reservation sample:
  - Reservation ID: `R200`
  - Guest: `Nimal Perera`
  - Room type: `DELUXE`
  - Check-in: `2025-11-01`
  - Check-out: `2025-11-03`
  - Expected nights: `2`
  - Expected total: `2 * 150 = 300`

## Automated Test Cases

| Test ID | Component | Input | Expected Result |
|---|---|---|---|
| UT-01 | DateUtil | 2025-11-01 to 2025-11-05 | Nights = 4 |
| UT-02 | DateUtil | same check-in/check-out | throws IllegalArgumentException |
| UT-03 | BillCalculator | nights=2, rate=100 | total=200 |
| UT-04 | ValidationUtil | invalid fields/contact/date | validation errors returned |
| IT-01 | ReservationDao | insert + find by ID | row persisted and fetched |
| IT-02 | API | login/add/view/bill flow | all operations succeed |

## TDD Evidence Notes

A TDD-friendly sequence was followed:
1. Defined expected outcomes for date, billing, and validation logic.
2. Wrote unit tests for these rules.
3. Implemented utility and service logic to satisfy tests.
4. Added DAO test to lock persistence contract.
5. Added API integration flow test to verify distributed behavior.

## Manual Test Checklist

1. Start API server.
2. Start Swing client.
3. Login with `admin/1234`.
4. Add reservation with valid data.
5. Search reservation by ID and verify details.
6. Generate bill and verify nights/rate/total.
7. Try invalid inputs:
   - non-numeric contact
   - checkout before checkin
   - duplicate reservation ID
8. Exit application and confirm no crash.

## Running Tests

```powershell
.\mvnw.cmd test
```

## Expected Outcome

All tests pass and build exits with success code.
