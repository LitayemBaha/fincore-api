# FinCore API

> Secure, scalable backend for enterprise financial data processing — **MVP**.
> Java 17 · Spring Boot 4 · Spring Data JPA · Spring Security (JWT) · Oracle (H2 for local dev).

---

## 1. Business Scenario

A fintech company is launching **FinCore API**, a backend platform that enables secure and
efficient financial data processing for enterprise applications. This repository delivers the
**Minimum Viable Product** demonstrating the core capabilities to internal stakeholders.

Core operations supported:

- Managing **accounts**
- Recording **transactions**
- Categorizing financial activity (**categories**)
- Providing aggregated **financial insights**

---

## 2. System Requirements

### 2.1 Functional (API capabilities)

| Area | Capability |
|------|-----------|
| Auth | Register, login, issue JWT access tokens |
| Accounts | Create, read, update, delete; per-user listing with pagination; running balance |
| Categories | CRUD; unique per user; typed as INCOME / EXPENSE |
| Transactions | CRUD; auto-adjust account balance; filter by account, category, type and date range; pagination |
| Insights | Overall overview (balances/income/expense), breakdown by category, monthly income-vs-expense trend |

### 2.2 Non-Functional

- **Security** — stateless JWT auth, BCrypt password hashing, per-user data isolation (ownership scoping on every query), method-level validation.
- **Performance** — pagination on all list endpoints, DB indexes on foreign keys and dates, aggregation pushed down to SQL, `open-in-view=false`.
- **Scalability** — stateless services (horizontally scalable), HikariCP connection pooling, layered separation of concerns.
- **Maintainability** — layered architecture, DTO/entity separation, centralized error handling, OpenAPI docs.

---

## 3. Architecture (Layered)

```
┌─────────────────────────────────────────────┐
│  Controller layer  (REST, validation, DTOs)  │  com.example.demo.controller
├─────────────────────────────────────────────┤
│  Service layer     (business logic, tx)       │  com.example.demo.service
├─────────────────────────────────────────────┤
│  Repository layer  (Spring Data JPA)          │  com.example.demo.repository
├─────────────────────────────────────────────┤
│  Domain / Entities (JPA model)                │  com.example.demo.model
└─────────────────────────────────────────────┘
   Cross-cutting: security (JWT), config, exception (global handler), dto
```

---

## 4. Data Model

```
User (1) ───< (N) Account (1) ───< (N) Transaction (N) >─── (1) Category
  │                                                               │
  └───────────────────────< (N) ────────────────────────────────┘
          (User owns Categories)
```

| Entity | Key fields | Relationships |
|--------|-----------|---------------|
| `User` | id, name, email (unique), passwordHash | owns Accounts & Categories |
| `Account` | id, name, type, currency, balance | belongs to User; has Transactions |
| `Category` | id, name, type (INCOME/EXPENSE) | belongs to User; classifies Transactions |
| `Transaction` | id, type, amount, description, date | belongs to Account; optional Category |

Tables are prefixed `FIN_` with indexes on `user_id`, `account_id`, `category_id`, and `tx_date`.

---

## 5. Running the Project

### Local (H2, default `dev` profile)

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
./mvnw spring-boot:run         

 # or: mvn spring-boot:run
```

- Swagger UI: http://localhost:8080/swagger-ui.html

### Oracle (`oracle` profile)

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=oracle
```

Configure via environment variables: `ORACLE_URL`, `ORACLE_USERNAME`, `ORACLE_PASSWORD`,
and `FINCORE_JWT_SECRET` (Base64, 256-bit).

---

## 6. API Overview

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register, returns JWT |
| POST | `/api/auth/login` | Login, returns JWT |
| GET/POST | `/api/accounts` | List / create accounts |
| GET/PUT/DELETE | `/api/accounts/{id}` | Manage an account |
| GET/POST | `/api/categories` | List / create categories |
| GET/PUT/DELETE | `/api/categories/{id}` | Manage a category |
| GET/POST | `/api/transactions` | List (filterable) / create transactions |
| GET/PUT/DELETE | `/api/transactions/{id}` | Manage a transaction |
| GET | `/api/insights/overview` | Totals + per-account balances |
| GET | `/api/insights/by-category` | Totals grouped by category |
| GET | `/api/insights/monthly` | Monthly income vs expense |

All endpoints except `/api/auth/**` require `Authorization: Bearer <token>`.

---

## 7. Testing

### 7.1 Automated Integration Tests (MockMvc)

Full-suite **functional testing** covering all endpoints, validation, and error scenarios using Spring Boot `@SpringBootTest` + `MockMvc` with isolated H2 database per test run.

**Run all tests:**
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn test
```

**Test classes & coverage:**

| Test Class | Focus | Scenarios |
|-----------|-------|-----------|
| `AuthControllerIntegrationTest` | Authentication & JWT flow | Register (201), duplicate email (409), invalid input (400), login (200), wrong password (401) |
| `AccountControllerIntegrationTest` | Account CRUD & ownership | Create (201), list paged, get, update, delete (204), validation (400), unknown id (404), cross-user access (403), no token (401) |
| `CategoryControllerIntegrationTest` | Category CRUD | Full lifecycle: create, list, get, update, delete; validation; ownership |
| `TransactionControllerIntegrationTest` | Transaction CRUD & balance logic | Create income/expense with **balance adjustment**, delete/update **reversal**, validation (400), filtered listing (account/category/type/date), unknown account (404) |
| `InsightControllerIntegrationTest` | Aggregated financial insights | `/overview`, `/by-category` (w/ date range), `/monthly`, auth required (401) |
| `SecurityIntegrationTest` | JWT & access control | No token (401), invalid token (401), public endpoints reachable |

**Expected result:**
```
[INFO] Tests run: 33, Failures: 0, Errors: 0, Skipped: 0
```

---

### 7.2 Manual HTTP Testing (requests.http)

Interactive endpoint testing via IDE HTTP client or tools like Postman/curl. Two files provided:

#### `requests.http` (Generic template)
Raw endpoint definitions (replace tokens/IDs manually):
```bash
cat requests.http
```

### 7.3 Database Seeding Scripts (db/scripts/)

Idempotent SQL scripts for populating test data into Oracle/H2.

**Script order:**
```
1. 01_cleanup.sql          — Delete all data (reverse FK order)
2. 02_seed_users.sql       — Insert 2 test users (Alice, Bob)
3. 03_seed_accounts.sql    — Insert accounts per user
4. 04_seed_categories.sql  — Insert INCOME/EXPENSE categories
5. 05_seed_transactions.sql — Insert sample transactions + balance adjustments
```


**Oracle (via container):**
```bash
docker exec fincore-oracle sqlplus fincore/fincore@XE < db/scripts/01_cleanup.sql
docker exec fincore-oracle sqlplus fincore/fincore@XE < db/scripts/02_seed_users.sql
# ... etc.
```


