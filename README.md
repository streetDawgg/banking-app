# Java Banking App (CLI + Spring Boot REST API)

Two Maven modules:(Three actually, GUI under Construction)

| Module   | What it is |
|----------|------------|
| `server` | Spring Boot REST API (Spring Security + JWT, JPA/Hibernate, PostgreSQL, Flyway, BCrypt) |
| `cli`    | Command-line client. It only talks to the server over HTTP, so a mobile app can replace it later with no backend changes. |

## Requirements
- JDK 25 and Maven 3.9+
- PostgreSQL (or Docker, see below)

## 1. Start PostgreSQL
```bash
docker compose up -d
```
Or use your own Postgres: create a database `bankdb` and set `DB_URL`, `DB_USER`, `DB_PASSWORD`
(defaults: `jdbc:postgresql://localhost:5432/bankdb`, `postgres`, `postgres`).

## 2. Start the server (terminal 1)
```bash
mvn -pl server spring-boot:run
```
Flyway creates the tables on first start (`server/src/main/resources/db/migration`).

## 3. Start the CLI (terminal 2)
```bash
mvn -q -pl cli compile exec:java
```
Set `BANK_API_URL` if the server isn't on `http://localhost:8080`.
If `₱` shows as `?` on Windows, run `chcp 65001` first.

Register two users in separate CLI sessions to try transfers.

## API
| Method | Endpoint | Notes |
|--------|----------|-------|
| POST | `/api/auth/register` | `{fullName, phoneNumber, pin}` |
| POST | `/api/auth/login` | `{phoneNumber, pin}` -> `{token}` |
| POST | `/api/auth/logout` | revokes the token (in-memory deny-list) |
| GET  | `/api/users/me` | profile |
| GET  | `/api/accounts/me` | account info |
| GET  | `/api/accounts/balance` | balance |
| POST | `/api/accounts/deposit` | `{amount}` (added for the CLI phase) |
| POST | `/api/accounts/withdraw` | `{amount}` (added for the CLI phase) |
| GET  | `/api/transactions/history?page=0&size=20` | newest first |
| GET  | `/api/transactions/recent` | last 5 |
| GET  | `/api/transfers/recipient/{phoneNumber}` | name lookup for confirmation |
| POST | `/api/transfers` | `{recipientPhoneNumber, amount, description?}` |

All endpoints except register/login need `Authorization: Bearer <token>`.
Errors look like `{"status": 400, "message": "..."}`.

## Rules implemented
- Phone: exactly 11 digits, unique. PIN: 4-6 digits, stored only as a BCrypt hash.
- 10-digit unique account number generated at registration; balance starts at 0.00.
- 3 wrong PINs -> account locked for **30 seconds** (`bank.security.lock-seconds`); counter resets on success.
- JWT access token, 15 minutes (`bank.jwt.expiration-minutes`).
- Money uses `BigDecimal` / `NUMERIC(19,2)`; DB `CHECK (balance >= 0)`.
- Transfers run in one DB transaction with row locks taken in a fixed order (no deadlocks); any failure rolls back everything.
- Insufficient-funds attempts are logged as `FAILED` transactions.
- Transaction rows can't be updated or deleted (DB trigger).

## Known simplifications (upgrade later)
- Weak PIN rules and no IP/phone rate limiting.
- Login error reveals remaining attempts, which also reveals that a phone number exists.
- Logout deny-list is in memory (cleared on restart).
- Default JWT secret is for development only; set `JWT_SECRET`.
- No idempotency key on transfers yet (retries could double-send).
- No automated tests yet.
