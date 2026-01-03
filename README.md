# Ember API - Enterprise Spring Boot REST API

A comprehensive enterprise-level REST API built with Spring Boot, featuring traditional authentication and OAuth2 Google login. This project demonstrates modern Java backend development practices with a clean, layered architecture.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Technology Stack](#technology-stack)
3. [Project Structure](#project-structure)
4. [Database Schema](#database-schema)
5. [API Endpoints](#api-endpoints)
6. [Setup Instructions](#setup-instructions)
7. [Understanding the Code](#understanding-the-code)
8. [OAuth2 Flow Explanation](#oauth2-flow-explanation)
9. [Best Practices Used](#best-practices-used)

---

## Architecture Overview

This project follows a **Layered Architecture** pattern, which separates concerns into distinct layers. Each layer has a specific responsibility and communicates only with adjacent layers.

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │  ← Controllers (Handle HTTP requests)
├─────────────────────────────────────────┤
│         Service Layer                   │  ← Business Logic
├─────────────────────────────────────────┤
│         Repository Layer                │  ← Data Access (JPA)
├─────────────────────────────────────────┤
│         Database Layer                  │  ← SQL Server Database
└─────────────────────────────────────────┘
```

### Why Layered Architecture?

- **Separation of Concerns**: Each layer has a single responsibility
- **Maintainability**: Easy to modify one layer without affecting others
- **Testability**: Each layer can be tested independently
- **Scalability**: Easy to add new features following the same pattern

---

## Technology Stack

### Core Framework

#### 1. **Spring Boot 4.0.1**
**What it does**: Spring Boot is a framework that simplifies Spring application development by providing auto-configuration and production-ready features.

**Why we use it**:
- Reduces boilerplate code
- Auto-configures common components (database, security, etc.)
- Embedded server (Tomcat) - no need to deploy WAR files
- Production-ready features (health checks, metrics)

**Example**: Instead of manually configuring a database connection with XML, Spring Boot does it automatically by reading `application.properties`.

---

#### 2. **Spring Boot Starter Web MVC**
**What it does**: Provides the infrastructure to build REST APIs using the Model-View-Controller pattern.

**Key Components**:
- `@RestController`: Marks a class as a REST API controller
- `@RequestMapping`: Maps HTTP requests to handler methods
- `@GetMapping, @PostMapping, @PutMapping, @DeleteMapping`: HTTP method-specific mappings
- Jackson: Automatically converts Java objects to JSON and vice versa

**Example**:
```java
@RestController
@RequestMapping("/auth")
public class AuthIdentityController {
    @PostMapping("/login")  // Maps to POST /auth/login
    public ResponseEntity<ApiResponse<UserResponse>> login(@RequestBody LoginRequest request) {
        // Spring automatically converts JSON to LoginRequest object
        return ResponseEntity.ok(userService.login(request));
    }
}
```

---

#### 3. **Spring Boot Starter Data JPA**
**What it does**: Java Persistence API - provides an abstraction layer for database operations.

**Key Features**:
- Object-Relational Mapping (ORM) - maps Java objects to database tables
- Eliminates need to write SQL queries for basic operations
- Automatic table creation based on entity classes
- Built-in pagination, sorting, and query methods

**Example**:
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // No implementation needed! Spring generates the SQL automatically
    Optional<User> findByEmail(String email);
    // This creates: SELECT * FROM users WHERE email = ?
}
```

**Without JPA**, you would write:
```java
String sql = "SELECT * FROM users WHERE email = ?";
PreparedStatement stmt = connection.prepareStatement(sql);
stmt.setString(1, email);
ResultSet rs = stmt.executeQuery();
// Manually map ResultSet to User object...
```

---

#### 4. **Spring Boot Starter Security**
**What it does**: Provides authentication and authorization for your application.

**Key Features**:
- User authentication (who are you?)
- Authorization (what can you do?)
- Protection against common attacks (CSRF, XSS, Session Fixation)
- Password encryption (BCrypt)
- OAuth2 support

**How it works**:
1. Every request goes through Security Filter Chain
2. Checks if user is authenticated
3. Checks if user has permission to access the resource
4. Encrypts passwords before storing in database

**Example**:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/auth/**").permitAll()  // Anyone can access
            .anyRequest().authenticated()             // Everything else needs login
        );
    }
}
```

---

#### 5. **Spring Boot Starter Validation**
**What it does**: Validates user input using annotations.

**Key Annotations**:
- `@NotBlank`: Field cannot be null or empty
- `@Email`: Must be a valid email format
- `@Size(min, max)`: String length constraints
- `@Min, @Max`: Number range constraints
- `@Pattern`: Regular expression validation

**Example**:
```java
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}

// In controller:
@PostMapping("/register")
public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
    // If validation fails, Spring automatically returns 400 Bad Request
    // with error details (handled by GlobalExceptionHandler)
}
```

---

#### 6. **Spring Boot Starter OAuth2 Client**
**What it does**: Enables "Login with Google" (or other OAuth2 providers) functionality.

**How OAuth2 works**:
1. User clicks "Login with Google"
2. Redirected to Google's login page
3. User enters Google credentials
4. Google redirects back to your app with an authorization code
5. Your app exchanges code for user information
6. You create/update user in your database

**Key Components**:
- `OAuth2UserService`: Processes user info from Google
- `OAuth2AuthenticationSuccessHandler`: What to do after successful login
- `OAuth2AuthenticationFailureHandler`: What to do if login fails

**Example Flow**:
```
User → /oauth2/authorization/google → Google Login Page →
Google authenticates → /login/oauth2/code/google →
CustomOAuth2UserService processes user →
OAuth2AuthenticationSuccessHandler → Redirect to frontend
```

---

#### 7. **Lombok**
**What it does**: Reduces boilerplate code using annotations.

**Key Annotations**:
- `@Data`: Generates getters, setters, toString, equals, hashCode
- `@NoArgsConstructor`: Generates no-argument constructor
- `@AllArgsConstructor`: Generates constructor with all fields
- `@RequiredArgsConstructor`: Generates constructor for final fields
- `@Getter, @Setter`: Individual getter/setter generation

**Without Lombok**:
```java
public class User {
    private Long id;
    private String email;

    // You must write all of this manually:
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    @Override
    public String toString() { return "User{id=" + id + ", email=" + email + "}"; }
    @Override
    public boolean equals(Object o) { /* ... */ }
    @Override
    public int hashCode() { /* ... */ }
}
```

**With Lombok**:
```java
@Data  // One annotation does all of the above!
public class User {
    private Long id;
    private String email;
}
```

---

#### 8. **Microsoft SQL Server JDBC Driver**
**What it does**: Allows Java to connect to SQL Server database.

**How it works**:
```properties
# application.properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=lilknig_ember
spring.datasource.username=lilknig_admin
spring.datasource.password=admin
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

The JDBC driver translates JPA operations into SQL Server-specific SQL commands.

---

#### 9. **Hibernate (comes with Spring Data JPA)**
**What it does**: The actual ORM implementation that JPA uses.

**Key Features**:
- Automatic table creation/updates (DDL generation)
- Lazy loading (load related data only when needed)
- Caching (reduce database queries)
- Transaction management

**Example**:
```properties
spring.jpa.hibernate.ddl-auto=update  # Auto-update tables when entities change
spring.jpa.show-sql=true              # Show SQL queries in console (for learning)
```

---

## Project Structure

```
src/main/java/com/lilknig/emberapi/
│
├── config/                              # Configuration Classes
│   ├── SecurityConfig.java              # Spring Security configuration
│   ├── CorsConfig.java                  # Cross-Origin Resource Sharing
│   └── WebConfig.java                   # Web MVC configuration
│
├── controller/                          # REST API Controllers (Presentation Layer)
│   └── auth/
│       └── AuthIdentityController.java  # Handles /auth/* endpoints
│
├── dto/                                 # Data Transfer Objects
│   ├── request/                         # Request DTOs (from client)
│   │   ├── LoginRequest.java            # Login form data
│   │   └── RegisterRequest.java         # Registration form data
│   └── response/                        # Response DTOs (to client)
│       ├── ApiResponse.java             # Generic API response wrapper
│       └── UserResponse.java            # User data (without password)
│
├── entity/                              # Database Entities (Domain Models)
│   ├── User.java                        # User table mapping
│   └── AuthProvider.java                # Enum: LOCAL, GOOGLE
│
├── exception/                           # Exception Handling
│   ├── BadRequestException.java         # 400 errors
│   ├── UnauthorizedException.java       # 401 errors
│   ├── ResourceNotFoundException.java   # 404 errors
│   └── GlobalExceptionHandler.java      # Centralized exception handling
│
├── repository/                          # Data Access Layer (Repository Layer)
│   └── UserRepository.java              # Database operations for User
│
├── security/                            # Security Components
│   └── oauth2/
│       ├── OAuth2UserInfo.java          # Abstract OAuth2 user data
│       ├── GoogleOAuth2UserInfo.java    # Google-specific user data
│       ├── CustomOAuth2User.java        # Custom OAuth2 principal
│       ├── CustomOAuth2UserService.java # Process OAuth2 users
│       ├── OAuth2AuthenticationSuccessHandler.java
│       └── OAuth2AuthenticationFailureHandler.java
│
├── service/                             # Service Layer (Business Logic)
│   ├── UserService.java                 # Interface defining user operations
│   └── impl/
│       └── UserServiceImpl.java         # Implementation of business logic
│
├── util/                                # Utility Classes
│   └── PasswordUtil.java                # Password encoding/validation
│
└── EmberApiApplication.java             # Main application entry point
```

### Layer Explanation

#### **1. Controller Layer (Presentation)**
**Purpose**: Handle HTTP requests and responses.

**Responsibilities**:
- Receive HTTP requests
- Validate request format (with `@Valid`)
- Call appropriate service methods
- Format responses (convert to JSON)

**Should NOT**:
- Contain business logic
- Access database directly
- Handle complex calculations

**Example**:
```java
@RestController
@RequestMapping("/auth")
public class AuthIdentityController {
    private final UserService userService;  // Delegate to service layer

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        UserResponse user = userService.login(request);  // Service handles logic
        return ResponseEntity.ok(ApiResponse.success("Login successful", user));
    }
}
```

---

#### **2. Service Layer (Business Logic)**
**Purpose**: Implement business rules and orchestrate operations.

**Responsibilities**:
- Validate business rules (e.g., "email must be unique")
- Coordinate multiple repository calls
- Handle transactions
- Perform calculations and transformations

**Example**:
```java
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserResponse register(RegisterRequest request) {
        // Business rule: Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Business logic: Hash password before storing
        User user = new User();
        user.setPassword(PasswordUtil.encode(request.getPassword()));

        // Delegate to repository for data access
        User savedUser = userRepository.save(user);

        // Transform entity to DTO
        return mapToUserResponse(savedUser);
    }
}
```

---

#### **3. Repository Layer (Data Access)**
**Purpose**: Interact with the database.

**Responsibilities**:
- CRUD operations (Create, Read, Update, Delete)
- Custom queries
- No business logic

**Example**:
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring generates implementation automatically
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
}
```

---

#### **4. Entity Layer (Domain Models)**
**Purpose**: Represent database tables as Java objects.

**Example**:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    // Hibernate maps this class to a database table
}
```

---

## Database Schema

### Users Table

```sql
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    provider VARCHAR(50),           -- 'LOCAL' or 'GOOGLE'
    provider_id VARCHAR(255),       -- Google user ID (for OAuth2)
    created_at DATETIME2,
    updated_at DATETIME2
);
```

**Field Explanations**:
- `id`: Auto-increment primary key
- `email`: Unique identifier for users
- `password`: BCrypt-hashed password (60 characters, empty for OAuth2 users)
- `name`: User's display name
- `provider`: How user registered (LOCAL = email/password, GOOGLE = OAuth2)
- `provider_id`: Google's unique ID for the user
- `created_at`: Timestamp when user registered
- `updated_at`: Timestamp of last profile update

---

## API Endpoints

### 1. User Registration

**Endpoint**: `POST /auth/register`

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "name": "John Doe"
}
```

**Validation Rules**:
- Email: Must be valid email format, not blank
- Password: Minimum 6 characters, not blank
- Name: 2-100 characters, not blank

**Success Response** (201 Created):
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe",
    "provider": "LOCAL",
    "createdAt": "2026-01-03T10:30:00"
  },
  "timestamp": "2026-01-03T10:30:00"
}
```

**Error Response** (400 Bad Request):
```json
{
  "success": false,
  "message": "Email already registered",
  "data": null,
  "timestamp": "2026-01-03T10:30:00"
}
```

---

### 2. User Login

**Endpoint**: `POST /auth/login`

**Request Body**:
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Success Response** (200 OK):
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe",
    "provider": "LOCAL",
    "createdAt": "2026-01-03T10:30:00"
  },
  "timestamp": "2026-01-03T10:30:00"
}
```

**Error Response** (401 Unauthorized):
```json
{
  "success": false,
  "message": "Invalid email or password",
  "data": null,
  "timestamp": "2026-01-03T10:30:00"
}
```

---

### 3. OAuth2 Google Login

**Endpoint**: `GET /oauth2/authorization/google`

**How to use**:
```html
<a href="http://localhost:8080/oauth2/authorization/google">
  Login with Google
</a>
```

**Flow**:
1. User clicks link → Redirected to Google
2. User logs in with Google → Google redirects back
3. Backend processes user data → Creates/updates user in database
4. Redirects to frontend with user info:
   ```
   http://localhost:3000/oauth2/redirect?userId=1&email=user@gmail.com&name=John&provider=GOOGLE
   ```

---

## Setup Instructions

### Prerequisites

- **Java 21** (required for Spring Boot 4.0.1)
- **Maven 3.6+**
- **SQL Server** (or modify to use H2 for development)
- **Google Cloud Account** (for OAuth2 credentials)

---

### Step 1: Clone and Build

```bash
# Clone the repository
git clone <repository-url>
cd ember-api

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

---

### Step 2: Configure Database

**Option A: SQL Server (Production)**

1. Create database:
```sql
CREATE DATABASE lilknig_ember;
```

2. Update `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=lilknig_ember;encrypt=false
spring.datasource.username=your_username
spring.datasource.password=your_password
```

**Option B: H2 (Development/Testing)**

Update `pom.xml`:
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

Update `application.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
```

---

### Step 3: Configure OAuth2 Google

1. **Create Google OAuth2 Credentials**:
   - Go to [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
   - Create new project (or select existing)
   - Navigate to "Credentials" → "Create Credentials" → "OAuth 2.0 Client ID"
   - Application type: "Web application"
   - Authorized redirect URIs: `http://localhost:8080/login/oauth2/code/google`
   - Copy Client ID and Client Secret

2. **Update application.properties**:
```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID_HERE
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET_HERE
```

---

### Step 4: Test the Application

**Test Registration**:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "name": "Test User"
  }'
```

**Test Login**:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

**Test Google OAuth2**:
Open browser: `http://localhost:8080/oauth2/authorization/google`

---

## Understanding the Code

### How Validation Works

When you use `@Valid` in a controller:

```java
@PostMapping("/register")
public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
    // If validation fails, Spring throws MethodArgumentNotValidException
    // GlobalExceptionHandler catches it and returns formatted error
}
```

**What happens**:
1. Spring reads `@NotBlank`, `@Email`, `@Size` annotations on `RegisterRequest`
2. Validates the incoming JSON against these rules
3. If invalid, throws `MethodArgumentNotValidException`
4. `GlobalExceptionHandler` catches exception:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
        MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
        String fieldName = ((FieldError) error).getField();
        String errorMessage = error.getDefaultMessage();
        errors.put(fieldName, errorMessage);
    });
    return new ResponseEntity<>(
        ApiResponse.error("Validation failed", errors),
        HttpStatus.BAD_REQUEST
    );
}
```

**Response**:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email should be valid",
    "password": "Password must be at least 6 characters"
  },
  "timestamp": "2026-01-03T10:30:00"
}
```

---

### How Password Hashing Works

**Registration**:
```java
// In UserServiceImpl
user.setPassword(PasswordUtil.encode(request.getPassword()));
// "password123" → "$2a$10$XYZ..." (60-character BCrypt hash)
```

**Login**:
```java
// In UserServiceImpl
if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
    throw new UnauthorizedException("Invalid email or password");
}
// Compares "password123" with "$2a$10$XYZ..."
// BCrypt hashes the input and compares the hashes
```

**Why BCrypt?**:
- Salted: Same password produces different hashes
- Slow: Protects against brute-force attacks
- Adaptive: Can increase complexity over time

**Example**:
```
Input: "password123"
Hash1: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
Hash2: $2a$10$bvIG6Nmid91Mu9RcmmWZfO5HJIMCT8riNW0hEp8f6/FuA2/mHZFpe
       ↑ Different every time due to random salt
```

---

### How Dependency Injection Works

**Without Dependency Injection**:
```java
public class UserServiceImpl {
    private UserRepository userRepository = new UserRepositoryImpl(); // Tight coupling
    // Hard to test, hard to change implementation
}
```

**With Dependency Injection (Spring)**:
```java
@Service
@RequiredArgsConstructor  // Lombok generates constructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;  // Injected by Spring
    // Spring automatically creates and injects UserRepository
}
```

**Benefits**:
- Loose coupling: Easy to swap implementations
- Testable: Can inject mock objects for testing
- Centralized configuration: Spring manages object creation

---

### How JPA Annotations Work

```java
@Entity                                    // Marks this as a database table
@Table(name = "users")                     // Table name in database
public class User {

    @Id                                    // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;

    @Column(nullable = false, unique = true)  // NOT NULL UNIQUE constraint
    private String email;

    @Enumerated(EnumType.STRING)           // Store enum as string ("LOCAL", "GOOGLE")
    private AuthProvider provider;

    @CreationTimestamp                     // Auto-set on insert
    @Column(updatable = false)             // Cannot be changed after creation
    private LocalDateTime createdAt;

    @UpdateTimestamp                       // Auto-update on every save
    private LocalDateTime updatedAt;
}
```

**Hibernate generates SQL**:
```sql
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    provider VARCHAR(50),
    created_at DATETIME2 NOT NULL,
    updated_at DATETIME2
);
```

---

## OAuth2 Flow Explanation

### Traditional Login Flow
```
Client → POST /auth/login → Controller → Service → Repository → Database
                                ↓
                         Password validation
                                ↓
                           Return user data
```

### OAuth2 Google Login Flow

```
1. Client clicks "Login with Google"
   ↓
2. Redirect to: http://localhost:8080/oauth2/authorization/google
   ↓
3. Spring Security redirects to: https://accounts.google.com/o/oauth2/v2/auth
   ↓
4. User enters Google credentials
   ↓
5. Google redirects to: http://localhost:8080/login/oauth2/code/google?code=AUTH_CODE
   ↓
6. Spring Security exchanges code for access token
   ↓
7. Spring Security fetches user info from Google: https://www.googleapis.com/oauth2/v3/userinfo
   ↓
8. CustomOAuth2UserService.loadUser() is called
   ↓
9. Extract user data (email, name, sub)
   ↓
10. Check if user exists in database
    ├─ Exists → Update user info
    └─ New → Create new user
   ↓
11. OAuth2AuthenticationSuccessHandler.onAuthenticationSuccess()
   ↓
12. Redirect to frontend: http://localhost:3000/oauth2/redirect?userId=1&email=...
```

### Code Walkthrough

**Step 1: User clicks login link**
```html
<a href="http://localhost:8080/oauth2/authorization/google">Login with Google</a>
```

**Step 2: Spring Security Configuration**
```java
// SecurityConfig.java
.oauth2Login(oauth2 -> oauth2
    .userInfoEndpoint(userInfo -> userInfo
        .userService(customOAuth2UserService)  // Called after Google returns user info
    )
    .successHandler(oAuth2AuthenticationSuccessHandler)  // Called on success
    .failureHandler(oAuth2AuthenticationFailureHandler)  // Called on failure
)
```

**Step 3: Process User Data**
```java
// CustomOAuth2UserService.java
@Override
public OAuth2User loadUser(OAuth2UserRequest userRequest) {
    OAuth2User oAuth2User = super.loadUser(userRequest);  // Get data from Google

    // Extract email, name, etc.
    OAuth2UserInfo userInfo = new GoogleOAuth2UserInfo(oAuth2User.getAttributes());

    // Find or create user in database
    User user = userRepository.findByEmail(userInfo.getEmail())
        .orElseGet(() -> registerNewUser(userInfo));

    return new CustomOAuth2User(oAuth2User, user);
}
```

**Step 4: Success Handler**
```java
// OAuth2AuthenticationSuccessHandler.java
@Override
public void onAuthenticationSuccess(...) {
    CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

    // Build redirect URL with user data
    String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
        .queryParam("userId", oAuth2User.getUser().getId())
        .queryParam("email", oAuth2User.getUser().getEmail())
        .build().toUriString();

    // Redirect to frontend
    response.sendRedirect(targetUrl);
}
```

---

## Best Practices Used

### 1. **DTO Pattern (Data Transfer Objects)**

**Why?**
- Decouples API contract from database structure
- Security: Don't expose password field
- Flexibility: Can combine data from multiple entities

**Example**:
```java
// Entity (internal, matches database)
@Entity
public class User {
    private Long id;
    private String email;
    private String password;  // Sensitive!
}

// Response DTO (external, sent to client)
public class UserResponse {
    private Long id;
    private String email;
    // No password field!
}
```

---

### 2. **Global Exception Handling**

**Why?**
- Consistent error responses across all endpoints
- Centralized error handling logic
- Clean controller code

**Example**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(BadRequestException ex) {
        return new ResponseEntity<>(
            ApiResponse.error(ex.getMessage()),
            HttpStatus.BAD_REQUEST
        );
    }
}
```

**Without GlobalExceptionHandler**, every controller would need:
```java
try {
    userService.register(request);
} catch (BadRequestException e) {
    return ResponseEntity.badRequest().body(/* format error */);
}
```

---

### 3. **Generic Response Wrapper**

**Why?**
- Consistent response format
- Easy to parse on frontend
- Include metadata (timestamp, success status)

**Example**:
```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;              // Generic type
    private LocalDateTime timestamp;
}

// Usage:
ApiResponse<UserResponse> response = ApiResponse.success(userResponse);
ApiResponse<List<User>> response = ApiResponse.success(userList);
ApiResponse<Void> response = ApiResponse.error("Not found");
```

---

### 4. **Interface-Based Service Layer**

**Why?**
- Easy to swap implementations
- Supports multiple implementations (e.g., caching decorator)
- Better for testing (mock the interface)

**Example**:
```java
// Interface
public interface UserService {
    UserResponse register(RegisterRequest request);
}

// Implementation 1: Database
@Service
public class UserServiceImpl implements UserService { }

// Implementation 2: With caching (future)
@Service
public class CachedUserService implements UserService { }
```

---

### 5. **Repository Pattern**

**Why?**
- Abstracts database access
- Easy to change database technology
- Built-in pagination, sorting

**Example**:
```java
public interface UserRepository extends JpaRepository<User, Long> {
    // Spring provides: save(), findById(), findAll(), delete(), etc.
    Optional<User> findByEmail(String email);
}
```

---

## Common Questions

### Q: Why use DTOs instead of returning entities directly?

**A**: Security and flexibility.

**Bad**:
```java
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id);  // Exposes password!
}
```

**Good**:
```java
@GetMapping("/users/{id}")
public UserResponse getUser(@PathVariable Long id) {
    User user = userRepository.findById(id);
    return mapToUserResponse(user);  // No password field
}
```

---

### Q: Why use @RestControllerAdvice instead of try-catch in every method?

**A**: DRY (Don't Repeat Yourself) principle.

**Without @RestControllerAdvice**:
```java
@PostMapping("/register")
public ResponseEntity register(...) {
    try {
        return userService.register(request);
    } catch (BadRequestException e) {
        return ResponseEntity.badRequest().body(formatError(e));
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(formatError(e));
    }
}

@PostMapping("/login")
public ResponseEntity login(...) {
    try {
        return userService.login(request);
    } catch (UnauthorizedException e) {
        return ResponseEntity.status(401).body(formatError(e));
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(formatError(e));
    }
}
// Repeated code everywhere!
```

**With @RestControllerAdvice**:
```java
// Controllers: clean, no try-catch
@PostMapping("/register")
public ResponseEntity register(...) {
    return userService.register(request);  // Exceptions handled automatically
}

// GlobalExceptionHandler: centralized
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity handleBadRequest(BadRequestException ex) { }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleGeneric(Exception ex) { }
}
```

---

### Q: Why use Lombok?

**A**: Reduces boilerplate, improves readability.

**Without Lombok** (150+ lines):
```java
public class User {
    private Long id;
    private String email;
    private String password;

    public User() { }

    public User(Long id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public boolean equals(Object o) { /* 20 lines */ }

    @Override
    public int hashCode() { /* 10 lines */ }

    @Override
    public String toString() { /* 10 lines */ }
}
```

**With Lombok** (10 lines):
```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String email;
    private String password;
}
```

---

## Next Steps for Learning

### Beginner Level
1. ✅ Understand layered architecture
2. ✅ Learn how validation works
3. ✅ Understand dependency injection
4. ✅ Study JPA annotations

### Intermediate Level
5. Add JWT token-based authentication
6. Implement refresh tokens
7. Add pagination to endpoints
8. Create unit tests with JUnit and Mockito
9. Add Swagger/OpenAPI documentation

### Advanced Level
10. Implement role-based authorization
11. Add Redis caching
12. Implement rate limiting
13. Add logging with SLF4J
14. Deploy to cloud (AWS, Azure, GCP)

---

## Useful Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [JPA/Hibernate Guide](https://www.baeldung.com/learn-jpa-hibernate)
- [OAuth2 Explanation](https://www.oauth.com/)
- [REST API Best Practices](https://restfulapi.net/)

---

## License

This project is for educational purposes.

---

## Contributing

Feel free to fork, modify, and learn from this project!

---

**Happy Learning! 🚀**

If you have questions, create an issue or reach out to the maintainers.
