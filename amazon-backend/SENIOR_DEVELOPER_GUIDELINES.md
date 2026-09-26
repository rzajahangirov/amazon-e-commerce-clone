# 🛡️ Senior Backend Developer Guidelines & Engineering Handbook

> **Project:** `amazon-backend` (Amazon Clone / Enterprise E-Commerce Platform)  
> **Tech Stack:** Spring Boot 3.x, Java 17+, PostgreSQL, Hibernate/JPA, Redis, Spring Security (JWT), Docker  
> **Target Audience:** All developers and AI agents contributing code to this repository.  
> **Rule of Thumb:** Every task, PR, and feature implementation **MUST** strictly adhere to the rules in this document.

---

## 📑 Table of Contents
1. [Core Architectural Principles](#1-core-architectural-principles)
2. [File & Package Structure Conventions](#2-file--package-structure-conventions)
3. [Universal API Response Contract (`ResponseDto<T>`)](#3-universal-api-response-contract-responsedtot)
4. [DTO Design & Strict Request Validation](#4-dto-design--strict-request-validation)
5. [JPA & Database Engineering (1+N, Indexing, Locking, Auditing)](#5-jpa--database-engineering-1n-indexing-locking-auditing)
6. [Transaction Management & Concurrency Control](#6-transaction-management--concurrency-control)
7. [Entity-DTO Mapping & Projections](#7-entity-dto-mapping--projections)
8. [Exception Handling & Standardized Error Codes](#8-exception-handling--standardized-error-codes)
9. [Logging, Clean Code & Method Design](#9-logging-clean-code--method-design)
10. [Security & User Context Management (The Principal Pattern)](#10-security--user-context-management-the-principal-pattern)
11. [Caching Strategy (Redis) & Async Processing](#11-caching-strategy-redis--async-processing)
12. [Production Readiness Roadmap (Rate Limiting, Security & UUID Strategy)](#12-production-readiness-roadmap-rate-limiting-security--uuid-strategy)
13. [Senior Developer Self-Review Checklist](#13-senior-developer-self-review-checklist)

---

## 1. Core Architectural Principles

### 1.1 Layered Architecture & Separation of Concerns
Strict unidirectional dependencies must be observed:
```
[Client / Frontend]
        │  (HTTP / JSON)
        ▼
[Controller Layer]       ── Validate inputs (@Valid), extract Principal credentials, return ResponseDto<T>
        │  (DTOs / Plain Java types)
        ▼
[Service Layer]          ── Business logic, transactions (@Transactional), mapping, domain events
        │  (Entities / Projections)
        ▼
[Repository Layer]       ── Spring Data JPA, database queries, specifications, projections
        │  (SQL / JDBC)
        ▼
[Database (PostgreSQL / Redis)]
```

### 1.2 Non-Negotiable Core Rules
- **Zero Entity Leakage:** Entities (`@Entity`) must **NEVER** leave the Service layer. Controllers must **NEVER** accept or return an Entity. All ingress and egress data must be wrapped in DTOs.
- **Dependency Injection:** Always use constructor injection with Lombok `@RequiredArgsConstructor` on `final` fields. **NEVER** use `@Autowired` on fields.
- **SOLID & DRY:**
  - **Single Responsibility (SRP):** Keep controllers thin and services focused on a single domain aggregate. If a service exceeds 200–300 lines, extract helper components or delegate to sub-services.
  - **Open/Closed (OCP):** Use interfaces, strategy patterns, or polymorphic services instead of massive `switch/case` or chained `if/else` statements.
  - **DRY:** Extract common logic into domain utilities or base classes. Do not duplicate validation or calculation algorithms across multiple services.
- **Language Policy:** All code comments, log messages, exception messages, Swagger descriptions, and commit messages **MUST be exclusively in English**.

---

## 2. File & Package Structure Conventions

### 2.1 Package Layout
Components are organized by architectural layer, with domain/feature sub-packages:
```
com.amazon/
├── config/              # Infrastructure beans (ModelMapper, CORS, Swagger, Redis, WebSocket)
├── controller/          # REST Controllers (@RestController)
├── dtos/                # Request & Response DTOs grouped by domain feature
│   └── [feature]/
│       ├── request/     # [Entity][Action]RequestDto.java
│       └── response/    # [Entity][Action]ResponseDto.java
├── entity/              # JPA Entities (@Entity) extending BaseEntity
├── enums/               # Domain enumerations (RoleType, OrderStatus, etc.)
├── exception/           # Custom runtime exceptions & GlobalExceptionHandler
├── payloads/            # Universal wrappers (ResponseDto, ApiResponse, PaginationPayload)
├── repository/          # Spring Data JPA repositories (@Repository)
├── security/            # JWT filters, UserDetails, SecurityConfig
└── service/             # Service interfaces & service/impl/ implementations
```

### 2.2 DTO Naming & Pathing Standard
Always follow this explicit convention:
```
dtos -> [feature] -> request  -> [Entity][Action]RequestDto.java
dtos -> [feature] -> response -> [Entity][Action]ResponseDto.java
```
*Examples:*
- `dtos/auth/request/LoginRequestDto.java`
- `dtos/auth/response/AuthResponseDto.java`
- `dtos/product/request/CreateProductRequestDto.java`
- `dtos/product/response/ProductDetailsResponseDto.java`

---

## 3. Universal API Response Contract (`ResponseDto<T>`)

### 3.1 Why `ResponseDto<T>` is Mandatory
Every single REST endpoint **MUST** return `ResponseEntity<ResponseDto<T>>`. Direct object returns or raw collections are strictly prohibited.
- **Null Safety:** Prevents frontend crashes when `data` is `null` or an empty list. The contract always contains predictable `{ "data": ..., "message": ... }`.
- **Uniform Client Contract:** Mobile apps and web frontends consume a standardized response wrapper across all microservices and endpoints.
- **Graceful Error Encapsulation:** Both successful results and business/validation errors adhere to the identical envelope structure.

### 3.2 Standard Envelope Schema
```json
{
  "data": { ... } | null,
  "message": "Operation completed successfully"
}
```

### 3.3 Factory Usage (`ApiResponse`)
Always use the static factory methods in `ApiResponse` rather than constructing `ResponseDto` manually:
```java
// ✅ DO: Return via ApiResponse factory
@GetMapping("/{id}")
public ResponseEntity<ResponseDto<ProductResponseDto>> getProductById(@PathVariable Long id) {
    ProductResponseDto result = productService.getProductById(id);
    return ResponseEntity.ok(ApiResponse.success(result, "Product retrieved successfully"));
}

// ❌ DON'T: Return raw DTO or construct ResponseDto manually
@GetMapping("/{id}")
public ProductResponseDto getProductByIdRaw(@PathVariable Long id) {
    return productService.getProductById(id); // VIOLATION
}
```

### 3.4 Pagination Contract
When returning paginated data, encapsulate it inside `PaginationPayload<T>`:
```java
ResponseDto<PaginationPayload<ProductResponseDto>> response = 
    ApiResponse.success(paginatedResult, "Products fetched successfully");
```
*`PaginationPayload` must contain:* `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `isLast`.

---

## 4. DTO Design & Strict Request Validation

### 4.1 Annotation Validation
Never trust client input. Every Request DTO must validate field bounds, formats, and nullability:
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequestDto {

    @NotBlank(message = "Product title is required")
    @Size(min = 3, max = 150, message = "Product title must be between 3 and 150 characters")
    private String title;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than zero")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
```

### 4.2 Mandatory Controller Validation
All request bodies must be marked with `@Valid`:
```java
@PostMapping
public ResponseEntity<ResponseDto<ProductResponseDto>> createProduct(
        @Valid @RequestBody CreateProductRequestDto request) {
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(productService.createProduct(request));
}
```

### 4.3 Validation Error Handling
Validation failures (`MethodArgumentNotValidException`) are intercepted by `GlobalExceptionHandler` and converted into a readable summary string:
```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ResponseDto<?>> handleValidationException(MethodArgumentNotValidException ex) {
    String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
}
```

---

## 5. JPA & Database Engineering (1+N, Indexing, Locking, Auditing)

### 5.1 Eliminating the N+1 Query Problem
- **Rule 1: Always `FetchType.LAZY`:** All `@ManyToOne` and `@OneToOne` relationships **MUST** explicitly specify `fetch = FetchType.LAZY`. Hibernate defaults `@ManyToOne` to `EAGER`, which causes disastrous cascading queries in production.
  ```java
  // ✅ DO:
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  // ❌ DON'T:
  @ManyToOne // Default is EAGER! Triggers N+1 queries.
  private Category category;
  ```
- **Rule 2: Fetch Joins & `@EntityGraph`:** When loading an entity along with its associations for read/write operations, use `@EntityGraph` or `JOIN FETCH`:
  ```java
  public interface OrderRepository extends JpaRepository<Order, Long> {

      @EntityGraph(attributePaths = {"user", "orderItems", "orderItems.product"})
      @Query("SELECT o FROM Order o WHERE o.id = :id")
      Optional<Order> findByIdWithDetails(@Param("id") Long id);
  }
  ```
- **Rule 3: Global Batch Fetching:** Ensure batch fetching is enabled in `application.properties`:
  ```properties
  spring.jpa.properties.hibernate.default_batch_fetch_size=25
  ```

### 5.2 Indexing Strategy
- **Foreign Keys:** Every foreign key column must have a database index to prevent full table scans during joins and deletes:
  ```java
  @Table(name = "products", indexes = {
      @Index(name = "idx_product_category_id", columnList = "category_id"),
      @Index(name = "idx_product_sku", columnList = "sku", unique = true),
      @Index(name = "idx_product_status_created", columnList = "status, created_at")
  })
  ```
- **High-Query Fields:** Add indexes on columns frequently used in `WHERE`, `ORDER BY`, or `JOIN` conditions.
- **Composite Indexes:** Order columns in composite indexes according to the **Leftmost Prefix** rule: place high-cardinality equality filters first, range filters last.

### 5.3 Lombok & JPA Safety Rules
- **NEVER use `@Data` or `@EqualsAndHashCode` on JPA Entities:**
  - `@Data` generates `equals()` and `hashCode()` using all fields. If an entity has lazy-loaded collections or circular relations, this triggers unexpected SQL queries or fatal `StackOverflowError`.
- **Correct Entity Template:**
  ```java
  @Entity
  @Table(name = "products")
  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @ToString(exclude = {"category", "reviews"}) // Exclude lazy relationships!
  public class Product extends BaseEntity {

      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      @Version // Optimistic locking!
      private Long version;

      @Override
      public boolean equals(Object o) {
          if (this == o) return true;
          if (!(o instanceof Product other)) return false;
          return id != null && id.equals(other.getId());
      }

      @Override
      public int hashCode() {
          return getClass().hashCode();
      }
  }
  ```

### 5.4 Optimistic Locking (`@Version`) for Race Condition Prevention
In high-concurrency systems (especially e-commerce: inventory, cart checkouts, balance updates), prevent race conditions using `@Version`:
```java
@Version
private Long version;
```
If two requests attempt to mutate stock simultaneously, the second transaction will fail with `OptimisticLockingFailureException`, preventing double-spending or overselling.

### 5.5 BaseEntity, Timestamps & Soft Delete Policy
- **BaseEntity Contract:** Every entity in the system **MUST extend `BaseEntity`**:
  ```java
  @MappedSuperclass
  @Getter
  @Setter
  public abstract class BaseEntity {

      @Column(name = "created_at", updatable = false)
      private LocalDateTime createdAt;

      @Column(name = "updated_at")
      private LocalDateTime updatedAt;

      @Column(name = "deleted_at")
      private LocalDateTime deletedAt;

      @PrePersist
      protected void onCreate() {
          this.createdAt = LocalDateTime.now();
          this.updatedAt = LocalDateTime.now();
      }

      @PreUpdate
      protected void onUpdate() {
          this.updatedAt = LocalDateTime.now();
      }
  }
  ```
- **Soft Delete Standard:**
  - In an enterprise e-commerce platform, business-critical records (Users, Orders, Products, Categories, Reviews) should **almost never be physically deleted** (Hard Delete) to maintain audit trails, financial history, and referential integrity.
  - **Standard Deletion:** Set `deletedAt = LocalDateTime.now()`. When filtering, query active records with `deletedAt IS NULL` or use Hibernate's `@SQLRestriction("deleted_at IS NULL")`.
  - **Rare Exceptions for Hard Delete:** Physical row deletion is strictly reserved for transient, temporary data (e.g., expired verification tokens, transient guest carts, temporary cache tables) or legal compliance under GDPR ("Right to be Forgotten").

---

## 6. Transaction Management & Concurrency Control

### 6.1 Transaction Scoping Rules
- **Read-Only Transactions:** Every read query method in services **MUST** be annotated with `@Transactional(readOnly = true)`:
  ```java
  @Transactional(readOnly = true)
  public ResponseDto<ProductResponseDto> getProduct(Long id) { ... }
  ```
  *Benefits:* Disables Hibernate dirty-checking, reduces memory overhead, and allows routing to read-replicas.
- **Write Transactions:** Keep write `@Transactional` methods as short as possible.
- **NEVER wrap external I/O inside `@Transactional`:**
  - Do not make HTTP calls, send emails, or upload files to AWS S3 inside an active `@Transactional` block. This holds the database connection open, starving the Hikari connection pool.
  ```java
  // ❌ BAD: S3 upload inside DB transaction
  @Transactional
  public void uploadProductImage(Long id, MultipartFile file) {
      String url = s3Service.upload(file); // Holds DB connection for seconds!
      productRepository.updateImageUrl(id, url);
  }

  // ✅ GOOD: Perform I/O first, then update DB in a quick transaction
  public void uploadProductImage(Long id, MultipartFile file) {
      String url = s3Service.upload(file); // DB connection is not acquired yet
      productService.saveImageUrl(id, url); // Short-lived transaction
  }
  ```

### 6.2 Idempotency for Mutating Operations
Financial and state-altering endpoints (e.g., `POST /api/v1/orders/checkout`) must accept an `Idempotency-Key` header stored in Redis to prevent duplicate charges or orders due to network retries.

---

## 7. Entity-DTO Mapping & Projections

### 7.1 Object Mapping
- Use **ModelMapper** or **MapStruct** for clean mapping between Entities and DTOs.
- Custom complex mappings should be placed in dedicated mapper classes or configuration methods rather than cluttered inside service methods.

### 7.2 Read Projections for High Performance
When querying large lists or summary reports, avoid fetching full JPA entities and mapping them in memory. Use Spring Data JPA interface-based or record-based projections:
```java
public interface ProductSummaryProjection {
    Long getId();
    String getTitle();
    BigDecimal getPrice();
    String getCategoryName();
}

@Query("SELECT p.id as id, p.title as title, p.price as price, c.name as categoryName " +
       "FROM Product p JOIN p.category c WHERE p.status = 'ACTIVE'")
List<ProductSummaryProjection> findAllActiveSummaries();
```

---

## 8. Exception Handling & Standardized Error Codes

### 8.1 Custom Exception Hierarchy
Create meaningful, descriptive domain exceptions extending `RuntimeException`:
- `ResourceNotFoundException` (HTTP 404)
- `DuplicateResourceException` (HTTP 409)
- `InvalidCredentialsException` (HTTP 401)
- `BusinessRuleException` (HTTP 422)
- `AccessDeniedException` (HTTP 403)

### 8.2 Enumerated Error Messages
Avoid hardcoding error strings across services. Store them in domain error enums:
```java
@Getter
@RequiredArgsConstructor
public enum ProductError {
    PRODUCT_NOT_FOUND("Product not found with id: "),
    INSUFFICIENT_STOCK("Insufficient stock for product id: "),
    DUPLICATE_SKU("A product with this SKU already exists: ");

    private final String message;
}
```

---

## 9. Logging, Clean Code & Method Design

### 9.1 Parameterized Logging with `@Slf4j`
- **Never use string concatenation** (`+`) inside log statements:
  ```java
  // ❌ BAD:
  log.info("Processing order " + orderId + " for user " + email);

  // ✅ GOOD:
  log.info("Processing order id: {} for user: {}", orderId, email);
  ```
- **Log Levels:**
  - `ERROR`: System crashes, unhandled exceptions, downstream service outages.
  - `WARN`: Recoverable business violations (e.g., invalid login attempt, rate limit exceeded).
  - `INFO`: Significant business milestones (e.g., user registered, order placed, payment confirmed).
  - `DEBUG`: Troubleshooting details (query execution, payload diagnostics).

### 9.2 Eliminating Magic Numbers & Strings
```java
// ❌ BAD:
if (retryCount > 3) { ... }
if (status.equals("ACT")) { ... }

// ✅ GOOD:
public static final int MAX_PAYMENT_RETRY_COUNT = 3;
if (retryCount > MAX_PAYMENT_RETRY_COUNT) { ... }
if (status == ProductStatus.ACTIVE) { ... }
```

### 9.3 Method Length & Readability
- Keep service methods small and focused (ideally under 25 lines).
- Avoid deep nesting (`if` within `for` within `if`). Use **Early Return (Guard Clauses)**:
  ```java
  // ✅ DO:
  if (!user.isActive()) {
      throw new BusinessRuleException("User account is deactivated");
  }
  // Proceed with main flow...
  ```

---

## 10. Security & User Context Management (The Principal Pattern)

### 10.1 Stateless Security Architecture
- Store no session state on the server. Authenticate every request via the stateless JWT filter (`JwtAuthFilter`).
- BCrypt is mandatory for password hashing (`PasswordEncoder`). Never store or log plain-text passwords.

### 10.2 Extracting Current Authenticated User (The Principal Rule)
When an endpoint requires the currently logged-in user:
1. **Controller Layer:** Inject `Principal principal` (from `java.security.Principal`).
2. **Controller Layer:** Extract `principal.getName()` (which in our JWT token represents the user's `email`).
3. **Controller Layer:** Pass **ONLY** the `String email` to the Service method. **NEVER pass the `Principal` or `UserDetails` object into the Service layer!**
4. **Service Layer:** Retrieve the user entity cleanly via `userRepository.findByEmail(email).orElseThrow(...)`.

#### Why this architectural separation is critical:
* **Clean Layer Decoupling:** The Service layer stays 100% pure business logic, free from web, servlet, or Spring Security dependencies.
* **Effortless Unit Testing:** Testing the service method requires passing a simple String (`"user@example.com"`). You never need to mock `Principal`, `Authentication`, or `SecurityContext` in service unit tests.

```java
// ✅ Controller: Injects Principal, passes ONLY email string to Service
@RestController
@RequestMapping("v1/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ResponseDto<UserProfileResponseDto>> getMyProfile(Principal principal) {
        String email = principal.getName();
        return ResponseEntity.ok(profileService.getProfileByEmail(email));
    }
}

// ✅ Service: Pure business logic accepting standard Java String
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public ResponseDto<UserProfileResponseDto> getProfileByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return ApiResponse.success(modelMapper.map(user, UserProfileResponseDto.class));
    }
}

// ❌ ANTI-PATTERN: Never do this!
@GetMapping("/me")
public ResponseEntity<?> getMyProfile(Principal principal) {
    return ResponseEntity.ok(profileService.getProfile(principal)); // VIOLATION: Service polluted with Security API
}
```

### 10.3 Method-Level Authorization
Enforce role permissions declaratively:
```java
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<ResponseDto<Void>> deleteProduct(@PathVariable Long id) { ... }
```

---

## 11. Caching Strategy (Redis) & Async Processing

### 11.1 Redis Caching Standards
- **Key Naming Convention:** Always namespace Redis keys with colons: `domain:identifier` (e.g., `products:123`, `categories:all`).
- **Always Configure TTL:** Never cache without a Time-To-Live. Stale or infinite cache is a leading cause of production memory outages.
- **Cache Invalidation:** Always evict cache upon mutating operations:
  ```java
  @Cacheable(value = "products", key = "#id")
  @Transactional(readOnly = true)
  public ProductResponseDto getProductById(Long id) { ... }

  @CacheEvict(value = "products", key = "#id")
  @Transactional
  public ProductResponseDto updateProduct(Long id, UpdateProductRequestDto request) { ... }
  ```

### 11.2 Safe Asynchronous Processing (`@Async`)
- Do not use Spring's default `SimpleAsyncTaskExecutor` (which spawns unbounded threads).
- Always define a custom, bounded `ThreadPoolTaskExecutor`:
  ```java
  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(5);
      executor.setMaxPoolSize(20);
      executor.setQueueCapacity(500);
      executor.setThreadNamePrefix("AsyncThread-");
      executor.initialize();
      return executor;
  }
  ```

---

## 12. Production Readiness Roadmap (Rate Limiting, Security & UUID Strategy)

As the application approaches production deployment, the following enterprise security and performance controls **MUST** be activated:

### 12.1 Rate Limiting & Brute-Force Password / Code Lockout
- **The Threat:** Attackers brute-forcing credentials, OTP codes, coupon redemptions, or flooding resource-intensive endpoints (search, checkout).
- **Rate Limiting Strategy:**
  - Implement a **Redis Token Bucket** (using `Bucket4j` + Redis) to enforce request quotas per IP and per authenticated user.
  - Sensitive endpoints (`/v1/api/auth/login`, `/v1/api/auth/register`, `/v1/api/auth/forgot-password`, `/v1/api/auth/verify-otp`) must have strict burst limits (e.g., maximum 5 attempts per minute).
- **Brute-Force Account / IP Lockout Mechanism:**
  - Track consecutive failed authentication attempts in Redis under key: `auth:failed:{email}` with a 15-minute TTL.
  - **Lockout Rule:** If a user enters an incorrect password or OTP **5 consecutive times**:
    - Temporarily lock the account/IP for a 15-minute cooldown window.
    - Subsequent attempts return `HTTP 429 Too Many Requests` (or `HTTP 423 Locked`) with a clear English message: `"Too many failed attempts. Your account is temporarily locked. Please try again after 15 minutes."`
  - On a successful login, immediately clear the Redis failure counter (`redisTemplate.delete("auth:failed:" + email)`).

### 12.2 Production Entity ID Strategy: UUID vs Sequential Long
- **The Vulnerability with Numeric Auto-Increment IDs in Production:**
  - Sequential IDs (`/v1/api/orders/1`, `/v1/api/users/42`) expose the application to **IDOR (Insecure Direct Object Reference)** attacks where malicious users iterate through IDs to scrape records.
  - Numeric IDs leak **critical business intelligence**: competitors can easily determine daily order counts, user growth rate, and transaction frequency simply by inspecting sequential ID deltas.
- **Senior Enterprise Solution (Dual-ID Pattern):**
  - **Internal Database Key (High Performance):** Retain `Long id` as the primary key (`@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`). PostgreSQL B-Tree clustered indexes and foreign key joins are up to 3x faster and consume significantly less memory with 64-bit integers than with 128-bit UUIDs.
  - **External Public Identifier (Zero Leakage):** Add a public `UUID uuid = UUID.randomUUID()` (or sequential `UUIDv7` / TSID) to entities, backed by a `UNIQUE` index.
  - **API Contract:** All public URLs, client payloads, and DTOs use only the `uuid` (`/v1/api/orders/{uuid}`). Controllers accept `UUID` in `@PathVariable`, and repositories lookup via `findByUuid(UUID uuid)`.
  - *Result:* Maximum database performance + absolute zero data/security leakage!

### 12.3 Production Environment & Secret Management
- **Zero Secrets in Git:** Never commit database credentials, JWT secrets, Redis passwords, or AWS keys into repository files.
- All secrets must be injected at runtime using OS Environment Variables or a Secret Manager (AWS Secrets Manager, Vault, or `.env` in Docker).

---

## 13. Senior Developer Self-Review Checklist

Before submitting code, asking for a review, or considering any feature complete, verify each question:

- [ ] **Response Structure:** Is the endpoint returning `ResponseEntity<ResponseDto<T>>` with `ApiResponse`?
- [ ] **No Leaked Entities:** Are all request inputs and response outputs pure DTOs?
- [ ] **Validation:** Are all DTO fields protected with Jakarta constraints and `@Valid` present in the controller?
- [ ] **Principal Rule:** In controllers, is `principal.getName()` extracted and passed to the service as `String email` (and NOT the `Principal` object)?
- [ ] **BaseEntity & Timestamps:** Does the entity extend `BaseEntity` with `createdAt`, `updatedAt`, and `deletedAt`?
- [ ] **Soft Delete Policy:** Is soft delete (`deletedAt`) used for business data, reserving hard delete only for transient/token data?
- [ ] **N+1 Avoided:** Are all relationships `FetchType.LAZY`? Did I use `@EntityGraph` or `JOIN FETCH` for associated queries?
- [ ] **Lombok Safety:** Did I avoid `@Data` on JPA entities? Are lazy relations excluded from `@ToString`?
- [ ] **Transactions:** Is `@Transactional(readOnly = true)` applied on all read methods? Are third-party I/O calls kept outside of write transactions?
- [ ] **Concurrency:** Does high-contention data (stock/balance) have `@Version` optimistic locking?
- [ ] **Indexes:** Are all Foreign Keys and frequently queried filter columns indexed?
- [ ] **Production Readiness:** Are sensitive endpoints designed with rate limiting / brute-force lockout in mind? Are external APIs using UUIDs instead of raw sequential IDs?
- [ ] **Logging:** Are log messages in English, using `@Slf4j` with `{}` placeholders (no string concatenation)?
- [ ] **Exceptions:** Are errors thrown as clear, custom domain runtime exceptions handled by `GlobalExceptionHandler`?
- [ ] **Dependencies:** Is constructor injection (`@RequiredArgsConstructor`) used on `final` fields?
- [ ] **English Only:** Are all comments, logs, and exception texts written exclusively in English?
