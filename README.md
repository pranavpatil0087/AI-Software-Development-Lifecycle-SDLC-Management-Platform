# AI SDLC Management Platform — Backend

**Status: Phase 0 (scaffolding) + Phase 1 (Authentication module) complete, pending your local build verification.**

## ⚠️ Important note on how this was built

This code was generated in a sandboxed environment with no access to `start.spring.io` or Maven Central,
so it was **not possible to run `mvn compile`/`mvn test` here** to prove a green build. Every dependency,
version, and API used was written from known-correct, stable Spring Boot 3.5.x / Java 21 usage — but per
your own process requirement ("verify every module compiles before moving on"), **you must run the steps
below locally and confirm success before we proceed to Phase 2.**

If anything fails to compile, paste me the exact Maven error and I'll fix it immediately — that's a normal
and expected part of this workflow given the constraint above.

## Tech stack (Phase 0/1 scope)

- Java 21, Spring Boot 3.5.0
- Spring Security + JWT (access + rotating refresh tokens)
- Spring Data JPA + Hibernate, PostgreSQL
- Flyway for schema migrations (no `ddl-auto=update`)
- MapStruct for DTO↔Entity mapping
- springdoc-openapi (Swagger UI)
- JUnit 5 + Mockito + Testcontainers

## Project structure

```
src/main/java/com/sdlcplatform/
├── config/        AppProperties, SecurityConfig, OpenApiConfig
├── security/       JwtService, JwtAuthenticationFilter, UserDetailsServiceImpl, AuthEntryPointJwt
├── controller/      AuthController
├── service/ (+impl) AuthService, EmailService
├── repository/      UserRepository, RoleRepository, RefreshTokenRepository, ...
├── entity/          BaseEntity, User, Role, RefreshToken, VerificationToken, PasswordResetToken
├── dto/             request/, response/
├── mapper/          UserMapper (MapStruct)
├── exception/       Custom exceptions + GlobalExceptionHandler
└── util/            TokenGenerator, HashUtil

src/main/resources/db/migration/   Flyway scripts (V1 schema, V2 seed roles)
```

## Local setup

1. Copy `.env.example` to `.env` and fill in real values (JWT secret, Mailtrap creds).
2. Start Postgres + backend:
   ```bash
   docker compose --env-file .env up --build
   ```
   Or run Postgres only and the backend via IDE/Maven:
   ```bash
   docker compose --env-file .env up postgres -d
   mvn spring-boot:run
   ```

## ✅ Verification checklist (run these before approving Phase 2)

1. **Build succeeds**
   ```bash
   mvn clean compile
   ```
2. **Unit tests pass** (JwtServiceTest, AuthServiceImplTest, and the Testcontainers-backed context test — Docker must be running for the last one)
   ```bash
   mvn test
   ```
3. **Docker image builds**
   ```bash
   docker build -t sdlc-platform-backend .
   ```
4. **Flyway migrations run cleanly** — on `docker compose up`, check logs for:
   ```
   Successfully applied 2 migrations to schema "public"
   ```
5. **Swagger is reachable and every auth endpoint is listed and testable**:
   `http://localhost:8080/swagger-ui.html`
   - `POST /api/v1/auth/register`
   - `POST /api/v1/auth/login`
   - `POST /api/v1/auth/refresh-token`
   - `POST /api/v1/auth/logout`
   - `GET  /api/v1/auth/verify-email`
   - `POST /api/v1/auth/forgot-password`
   - `POST /api/v1/auth/reset-password`
   - `PATCH /api/v1/auth/change-password`
6. **Manual smoke test**: register a user → check Mailtrap inbox for the verification email → hit
   `verify-email?token=...` → log in → confirm you get an access + refresh token pair → call
   `change-password` with the access token as a Bearer token and confirm it succeeds.

## What happens after you verify

Tell me the results of the checklist above. If everything is green, we move to **Phase 2: User Management**
(admin CRUD on users, deactivation, role assignment, profile/skills/department). If anything fails, send me
the error and I'll patch it before we move forward — per your process, we don't skip ahead on a red build.
