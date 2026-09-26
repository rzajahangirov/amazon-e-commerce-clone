# 🛒 Amazon E-Commerce Platform Clone

A modern, production-grade enterprise full-stack e-commerce platform inspired by Amazon, engineered with high scalability, clean layered architecture, and strict security standards.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x%20%2F%204.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Security](https://img.shields.io/badge/Security-JWT%20%2B%20Spring%20Security-blue.svg)](https://spring.io/projects/spring-security)
[![Database](https://img.shields.io/badge/Database-PostgreSQL-336791.svg)](https://www.postgresql.org/)
[![Cache](https://img.shields.io/badge/Cache-Redis-DC382D.svg)](https://redis.io/)
[![API Docs](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D.svg)](http://localhost:8080/swagger-ui.html)

---

## 📑 Table of Contents
- [Project Overview](#-project-overview)
- [Monorepo Architecture](#-monorepo-architecture)
- [Backend Engineering](#-backend-engineering)
  - [Tech Stack](#tech-stack)
  - [Package Layout](#package-layout)
  - [Authentication & JWT](#authentication--jwt)
  - [Unified API Response Contract](#unified-api-response-contract)
  - [Environment Variables](#environment-variables)
  - [Running the Backend Locally](#running-the-backend-locally)
- [Frontend Development](#-frontend-development)
- [Engineering Guidelines & Quality Standards](#-engineering-guidelines--quality-standards)

---

## 🌐 Project Overview

This repository is built as a modular monorepo containing both the backend service and the frontend client for an Amazon-style e-commerce ecosystem:
- **`amazon-backend`**: Robust Spring Boot microservice/monolith foundation handling user authentication, product catalogs, order processing, payments, real-time messaging, and media management.
- **`amazon-frontend`**: Modern, highly dynamic web interface mimicking Amazon's rich consumer shopping experience.

---

## 📂 Monorepo Architecture

```
amazon-e-commerce-clone/
├── README.md                          # Master project documentation
├── .gitignore                         # Root Git ignore rules
├── amazon-backend/                    # Spring Boot REST API
│   ├── src/
│   │   ├── main/java/com/amazon/      # Application source code
│   │   └── main/resources/            # Configuration (application.yaml)
│   ├── SENIOR_DEVELOPER_GUIDELINES.md # Enterprise backend guidelines & checklist
│   ├── pom.xml                        # Maven dependencies & build setup
│   └── mvnw, mvnw.cmd                 # Maven wrapper scripts
└── amazon-frontend/                   # Frontend client web application
    └── ...
```

---

## ⚙️ Backend Engineering

### Tech Stack

| Technology | Version / Spec | Purpose |
|---|---|---|
| **Java** | 21 (LTS) | Core language |
| **Spring Boot** | 3.x / 4.x | Framework & auto-configuration |
| **Spring Security** | Latest | Stateless authentication & authorization |
| **JWT (jjwt)** | 0.12.6 | Secure token generation & verification |
| **Spring Data JPA** | Latest | Database ORM & query management |
| **PostgreSQL** | 15+ | Relational data persistence |
| **Spring Data Redis** | Latest | High-speed caching & session state |
| **Spring WebSocket** | STOMP | Real-time notifications & updates |
| **ModelMapper** | 3.2.3 | Entity ↔ DTO transformation |
| **SpringDoc OpenAPI**| 3.0.3 | Swagger UI documentation |
| **AWS SDK (S3)** | 2.25.15 | Cloud storage for product assets & media |
| **Spring Actuator** | Latest | Health checks, metrics & monitoring |

---

### Package Layout

The backend strictly follows a layered architecture with zero entity leakage:

```
com.amazon/
├── config/                          # Infrastructure beans (ModelMapper, CORS, Swagger, Redis, WebSocket)
├── controller/                      # REST Controllers (thin layer, input validation)
├── dtos/                            # Data Transfer Objects
│   ├── auth/
│   │   ├── request/                 # RegisterRequestDto, LoginRequestDto
│   │   └── response/                # AuthResponseDto
│   └── user/
│       ├── request/                 # User request contracts
│       └── response/                # User response projections
├── entity/                          # JPA Entities extending BaseEntity
│   ├── BaseEntity.java              # Audit timestamps (createdAt, updatedAt, deletedAt)
│   └── User.java                    # User model with role-based access
├── enums/                           # Domain enums (RoleType, OrderStatus, etc.)
├── exception/                       # Custom runtime exceptions & GlobalExceptionHandler
├── payloads/                        # Universal API response wrappers
│   ├── ResponseDto.java             # Standard envelope: { data, message }
│   ├── ApiResponse.java             # Static factory methods for responses
│   └── PaginationPayload.java       # Standard pagination wrapper
├── repository/                      # Spring Data JPA repositories
├── security/                        # JwtAuthFilter, JwtService, SecurityConfig, UserDetails
├── service/                         # Business logic interfaces
│   └── impl/                        # Service implementations (@Transactional)
└── AmazonCloneApplication.java      # Application entry point
```

---

### Authentication & JWT

The platform comes pre-configured with a secure, stateless JWT authentication system:

#### 1. Register User
- **POST** `/v1/api/auth/register`

```json
{
  "name": "Jane",
  "surname": "Doe",
  "email": "jane@example.com",
  "password": "SecurePassword123!"
}
```

#### 2. Login User
- **POST** `/v1/api/auth/login`

```json
{
  "email": "jane@example.com",
  "password": "SecurePassword123!"
}
```

#### Response Envelope:
```json
{
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "name": "Jane",
      "surname": "Doe",
      "email": "jane@example.com",
      "role": "USER"
    }
  },
  "message": "Authentication successful"
}
```

---

### Unified API Response Contract

Every endpoint strictly returns `ResponseEntity<ResponseDto<T>>` to ensure predictable client consumption:

```json
// Success Response
{
  "data": { ... },
  "message": "Operation successful"
}

// Error Response
{
  "data": null,
  "message": "Error details or business rule violation"
}
```

---

### Environment Variables

| Variable | Default Value | Description |
|---|---|---|
| `DB_USERNAME` | `postgres` | PostgreSQL username |
| `DB_PASSWORD` | `12345` | PostgreSQL password |
| `JWT_SECRET` | `amazon-clone-dev-secret...` | Secret key for JWT signature (min. 32 chars) |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP email server |
| `MAIL_PORT` | `587` | SMTP port |
| `MAIL_USERNAME` | — | SMTP username / address |
| `MAIL_PASSWORD` | — | SMTP application password |
| `REDIS_HOST` | `localhost` | Redis server host |
| `REDIS_PORT` | `6379` | Redis server port |
| `AWS_S3_BUCKET` | `my-bucket` | AWS S3 bucket name |
| `AWS_REGION` | `eu-central-1` | AWS region |
| `AWS_ACCESS_KEY_ID` | — | AWS access key |
| `AWS_SECRET_ACCESS_KEY`| — | AWS secret key |

---

### Running the Backend Locally

#### 1. Prerequisites
- **Java 21** installed (`java -version`)
- **PostgreSQL** running on `localhost:5432`
  ```sql
  CREATE DATABASE amazon_clone_db;
  ```
- **Redis** running on `localhost:6379` (optional for base auth)

#### 2. Build & Launch
```bash
cd amazon-backend

# Build without executing tests
./mvnw clean compile

# Start Spring Boot application
./mvnw spring-boot:run
```

- **Base URL:** `http://localhost:8080`
- **Interactive Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

## 🎨 Frontend Development

The frontend application resides in `amazon-frontend/`. It is structured to interface with the `amazon-backend` REST endpoints, utilizing modern component architecture, responsive Amazon-style UI elements, cart state management, and real-time order tracking.

---

## 🛡️ Engineering Guidelines & Quality Standards

All contributions, whether human or AI-assisted, must strictly adhere to the guidelines documented in:
👉 [Senior Developer Guidelines](amazon-backend/SENIOR_DEVELOPER_GUIDELINES.md)

**Key Non-Negotiables:**
- **Zero Entity Leakage:** Entities never pass through the Controller layer.
- **DTO Validation:** All incoming payloads are strictly validated using `@Valid` and Jakarta constraints.
- **Transactional Discipline:** Clear separation between read-only (`@Transactional(readOnly = true)`) and state-mutating methods.
- **N+1 Query Prevention:** Proper use of entity graphs and `JOIN FETCH` queries.
