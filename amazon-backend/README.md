# Amazon Clone Backend

A production-ready Spring Boot backend for an Amazon Clone platform with JWT authentication, role-based security, and a clean layered architecture.

> 🛡️ **Engineering Standards:** All code contributions and AI prompts must strictly follow the [Senior Developer Guidelines](SENIOR_DEVELOPER_GUIDELINES.md) covering architecture, `ResponseDto` standards, DTO validation, 1+N query prevention, indexing, optimistic locking, and clean code principles.

## 🚀 Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Language |
| Spring Boot | 4.1.0 | Framework |
| Spring Security | — | Authentication & Authorization |
| Spring Data JPA | — | ORM / Database Access |
| PostgreSQL | — | Primary Database |
| JWT (jjwt) | 0.12.6 | Token-based Authentication |
| ModelMapper | 3.2.3 | Entity ↔ DTO Mapping |
| SpringDoc OpenAPI | 3.0.3 | Swagger UI / API Documentation |
| Lombok | — | Boilerplate Reduction |
| Spring WebSocket | — | Real-time Communication |
| Spring Mail | — | Email Service |
| Spring Data Redis | — | Caching |
| AWS S3 SDK | 2.25.15 | File Storage |
| Apache PDFBox | 3.0.4 | PDF Generation |
| Spring Actuator | — | Health & Metrics |

## 📁 Project Structure

```
src/main/java/com/amazon/
├── config/                          # Configuration classes
│   ├── ModelMapperConfig.java       # Entity-DTO mapping config
│   ├── SwaggerConfig.java          # OpenAPI/Swagger config
│   ├── WebConfig.java              # CORS & static resources
│   ├── WebSocketConfig.java        # WebSocket STOMP broker
│   └── WebSocketAuthInterceptor.java # WS JWT auth
├── controller/                      # REST Controllers
│   └── AuthController.java         # Register & Login endpoints
├── dtos/                           # Data Transfer Objects
│   ├── auth/
│   │   ├── request/                # RegisterRequestDto, LoginRequestDto
│   │   └── response/               # AuthResponseDto
│   └── user/
│       ├── request/                # User-specific request DTOs
│       └── response/               # UserResponseDto
├── entity/                         # JPA Entities
│   ├── BaseEntity.java             # Audit fields (createdAt, updatedAt, deletedAt)
│   └── User.java                   # User entity with role
├── enums/                          # Enum types
│   ├── RoleType.java               # ADMIN, USER
│   └── StatusType.java             # ACTIVE, INACTIVE, PENDING
├── exception/                      # Custom exceptions & handler
│   ├── BusinessRuleException.java
│   ├── DuplicateResourceException.java
│   ├── InvalidCredentialsException.java
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java # Central error handling
├── payloads/                       # API response wrappers
│   ├── ApiPayload.java             # Marker interface for request DTOs
│   ├── ApiResponse.java            # Static factory for ResponseDto
│   ├── AuthError.java              # Auth error message enum
│   ├── PaginationPayload.java      # Pagination wrapper
│   └── ResponseDto.java            # Universal response wrapper
├── repository/                     # JPA Repositories
│   └── UserRepository.java
├── security/                       # Security layer
│   ├── JwtAuthFilter.java          # JWT request filter
│   ├── JwtService.java             # Token generation & validation
│   ├── SecurityConfig.java         # Security filter chain
│   └── UserDetailsServiceImpl.java # Custom UserDetailsService
├── service/                        # Service interfaces
│   └── AuthService.java
├── service/impl/                   # Service implementations
│   └── AuthServiceImpl.java
└── AmazonCloneApplication.java     # Main entry point
```

## 🔐 Authentication

The template includes a complete JWT-based authentication system:

- **POST** `v1/api/auth/register` — Register a new user
- **POST** `v1/api/auth/login` — Login with credentials

### Register Request
```json
{
  "name": "John",
  "surname": "Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

### Login Request
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```

### Response
```json
{
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "user": {
      "id": 1,
      "name": "John",
      "surname": "Doe",
      "email": "john@example.com",
      "role": "USER"
    }
  },
  "message": "Registration successful"
}
```

## ⚙️ Configuration

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_USERNAME` | postgres | Database username |
| `DB_PASSWORD` | 12345 | Database password |
| `JWT_SECRET` | dev-secret... | JWT signing secret |
| `MAIL_HOST` | smtp.gmail.com | SMTP host |
| `MAIL_PORT` | 587 | SMTP port |
| `MAIL_USERNAME` | — | SMTP username |
| `MAIL_PASSWORD` | — | SMTP password |
| `REDIS_HOST` | localhost | Redis host |
| `REDIS_PORT` | 6379 | Redis port |
| `AWS_S3_BUCKET` | my-bucket | S3 bucket name |
| `AWS_REGION` | eu-central-1 | AWS region |
| `AWS_ACCESS_KEY_ID` | — | AWS access key |
| `AWS_SECRET_ACCESS_KEY` | — | AWS secret key |

### Prerequisites

- Java 21+
- PostgreSQL (running on localhost:5432)
- Create a database: `CREATE DATABASE spring_base_db;`

## 🏃 Running Locally

```bash
# Build the project
./mvnw clean install -DskipTests

# Run the application
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui.html`

## 📋 API Response Format

All endpoints return responses in a consistent format:

```json
{
  "data": { ... },
  "message": "Successful operation"
}
```

Error responses:
```json
{
  "data": null,
  "message": "Error description"
}
```

## 🛡️ Security Note

Currently all endpoints are set to `permitAll()` for development convenience. Update `SecurityConfig.java` to restrict endpoints as needed:

```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/v1/api/auth/**").permitAll()
    .anyRequest().authenticated())
```

## 📄 License

This project is a reusable template — free to use for any project.
