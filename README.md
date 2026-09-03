# Spring Boot 3 JWT Security

A Spring Boot REST API demonstrating JWT authentication, role-based authorization, and MongoDB persistence.

## Features

- User registration and login
- Access and refresh JWT tokens
- Admin and manager authorization
- Book and history endpoints
- MongoDB auditing
- Swagger/OpenAPI documentation

## Run

Set `MONGODB_ATLAS_URI`, or run MongoDB locally using the default connection:

```bash
./mvnw spring-boot:run
```

Run tests:

```bash
./mvnw test
```

Swagger UI: `http://localhost:8080/swagger-ui.html`

## Authentication

```text
POST /api/v1/auth/register
POST /api/v1/auth/authenticate
POST /api/v1/auth/refresh-token
POST /api/v1/auth/logout
```

Use the returned access token for protected endpoints:

```text
Authorization: Bearer <access-token>
```
