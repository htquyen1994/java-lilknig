# API Security Configuration Guide - Best Practices

This guide explains how to configure protected and unprotected API endpoints in the Ember API.

## Table of Contents
- [Security Architecture](#security-architecture)
- [Endpoint Configuration](#endpoint-configuration)
- [Method-Level Security](#method-level-security)
- [Adding New Endpoints](#adding-new-endpoints)
- [Environment Configuration](#environment-configuration)
- [Best Practices](#best-practices)

---

## Security Architecture

### Security Layers

1. **Global Configuration** (`SecurityConfig.java`)
   - Defines URL-based access rules
   - Configures OAuth2 authentication
   - Sets up CORS policies

2. **Constants** (`SecurityConstants.java`)
   - Centralized security configuration
   - Easy to maintain and update
   - Single source of truth

3. **Method-Level Security** (Annotations)
   - Fine-grained control on specific methods
   - Role-based access control (RBAC)

### Security Principles

✅ **Security by Default**: All endpoints require authentication unless explicitly permitted
✅ **Principle of Least Privilege**: Grant minimum necessary permissions
✅ **Defense in Depth**: Multiple layers of security
✅ **Separation of Concerns**: Public vs Protected vs Admin endpoints

---

## Endpoint Configuration

### Current Endpoint Security

#### 🔓 Public Endpoints (No Authentication)
```
POST /api/v1/auth/register       - User registration
POST /api/v1/auth/login          - User login
     /oauth2/**                  - OAuth2 flows (Google, etc.)
     /login/oauth2/**            - OAuth2 callbacks
     /error                      - Error page
GET  /actuator/health            - Health check
```

#### 🔐 Protected Endpoints (Requires Authentication)
```
GET  /api/v1/users               - Get all users
GET  /api/v1/users/{id}          - Get user by ID
     /api/v1/profile/**          - User profile management
```

#### 👑 Admin Only Endpoints (Requires ADMIN role)
```
     /api/v1/admin/**            - Admin panel
```

#### 🛠️ Development Only (Only in dev/local profiles)
```
     /h2-console/**              - H2 Database Console
     /swagger-ui/**              - Swagger Documentation
     /v3/api-docs/**             - OpenAPI Docs
     /actuator/**                - Spring Boot Actuator
```

---

## Method-Level Security

In addition to URL-based security, you can use annotations on controller methods or service methods.

### Available Annotations

#### @PreAuthorize
Most flexible - supports SpEL expressions

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    // Only authenticated users
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        // ...
    }

    // Only admin role
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // ...
    }

    // User can only access their own data or admin can access any
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
        @PathVariable Long id,
        @RequestBody UserUpdateRequest request
    ) {
        // ...
    }

    // Multiple roles
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PostMapping("/moderate")
    public ResponseEntity<Void> moderateContent() {
        // ...
    }
}
```

#### @Secured
Simple role checking (no SpEL)

```java
@Secured("ROLE_ADMIN")
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    // ...
}

@Secured({"ROLE_ADMIN", "ROLE_MODERATOR"})
@PostMapping("/moderate")
public ResponseEntity<Void> moderateContent() {
    // ...
}
```

#### @RolesAllowed
JSR-250 standard annotation

```java
@RolesAllowed("ADMIN")
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    // ...
}
```

---

## Adding New Endpoints

### Step 1: Determine Security Level

Ask yourself:
1. Should this be **public** (anyone can access)?
2. Should this require **authentication** (any logged-in user)?
3. Should this require **specific roles** (admin, moderator, etc.)?

### Step 2: Add to SecurityConstants

Edit `SecurityConstants.java`:

```java
public static final String[] PUBLIC_ENDPOINTS = {
    API_V1 + "/auth/**",
    API_V1 + "/products/public/**",  // ← Add your new public endpoint
    // ...
};

public static final String[] AUTHENTICATED_ENDPOINTS = {
    API_V1 + "/users/**",
    API_V1 + "/orders/**",  // ← Add your new protected endpoint
    // ...
};

public static final String[] ADMIN_ENDPOINTS = {
    API_V1 + "/admin/**",
    API_V1 + "/reports/**",  // ← Add your new admin endpoint
    // ...
};
```

### Step 3: (Optional) Add Method-Level Security

For fine-grained control, add annotations:

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getMyOrders() {
        // User can only see their own orders
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        // Only admins can see all orders
    }
}
```

---

## Environment Configuration

### Development Environment

`application-dev.properties`:
```properties
spring.profiles.active=dev

# Development CORS - Allow localhost
app.cors.allowed-origins=http://localhost:3000,http://localhost:4200,http://localhost:5173

# Development endpoints enabled automatically
```

### Production Environment

`application-prod.properties`:
```properties
spring.profiles.active=prod

# Production CORS - Specific domains only
app.cors.allowed-origins=https://yourdomain.com,https://app.yourdomain.com

# IMPORTANT: Development endpoints (H2, Swagger, Actuator) are automatically disabled
```

---

## Best Practices

### ✅ DO

1. **Use Constants**
   ```java
   // Good - Easy to maintain
   .requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS).permitAll()
   ```

2. **Order Matters**
   ```java
   // Most specific rules first
   auth.requestMatchers("/api/v1/admin/**").hasRole("ADMIN");
   auth.requestMatchers("/api/v1/users/**").authenticated();
   auth.anyRequest().authenticated(); // Generic rule last
   ```

3. **Use Environment Profiles**
   ```java
   if (isDevelopmentEnvironment()) {
       auth.requestMatchers(SecurityConstants.DEV_ENDPOINTS).permitAll();
   }
   ```

4. **Document Security Decisions**
   ```java
   // Allow public access to product catalog for SEO and performance
   .requestMatchers("/api/v1/products/public/**").permitAll()
   ```

5. **Use HTTPS in Production**
   ```properties
   # application-prod.properties
   server.ssl.enabled=true
   ```

### ❌ DON'T

1. **Don't Hardcode Endpoints**
   ```java
   // Bad - Hard to maintain
   .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register").permitAll()
   ```

2. **Don't Use permitAll() Carelessly**
   ```java
   // Dangerous!
   auth.anyRequest().permitAll() // Never do this!
   ```

3. **Don't Mix Security Approaches**
   ```java
   // Confusing - Use either URL-based OR method-level, not both for same endpoint
   ```

4. **Don't Expose Admin Endpoints in Production**
   ```java
   // Bad
   .requestMatchers("/h2-console/**").permitAll() // Remove in production
   ```

5. **Don't Use Wildcards for CORS in Production**
   ```java
   // Dangerous!
   configuration.setAllowedOrigins(Arrays.asList("*")); // Never in production!
   ```

---

## Examples

### Example 1: Public Product Catalog, Protected Checkout

```java
// SecurityConstants.java
public static final String[] PUBLIC_ENDPOINTS = {
    API_V1 + "/products/**",      // Anyone can view products
    API_V1 + "/categories/**",    // Anyone can view categories
};

public static final String[] AUTHENTICATED_ENDPOINTS = {
    API_V1 + "/cart/**",          // Must be logged in to manage cart
    API_V1 + "/checkout/**",      // Must be logged in to checkout
};
```

### Example 2: User Can Only Edit Their Own Profile

```java
@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateProfile(
        @PathVariable Long userId,
        @RequestBody ProfileUpdateRequest request
    ) {
        // Users can only update their own profile, unless they're admin
    }
}
```

### Example 3: Different Endpoints for Different Roles

```java
@RestController
@RequestMapping("/api/v1/content")
public class ContentController {

    @GetMapping("/public")
    public ResponseEntity<List<Content>> getPublicContent() {
        // Anyone can access
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/premium")
    public ResponseEntity<List<Content>> getPremiumContent() {
        // Only logged-in users
    }

    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    @PostMapping("/moderate")
    public ResponseEntity<Void> moderateContent(@RequestBody ModerationRequest request) {
        // Only moderators and admins
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) {
        // Only admins can delete
    }
}
```

---

## Testing Security

### Test Public Endpoints
```bash
# Should work without authentication
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass123","name":"Test User"}'
```

### Test Protected Endpoints
```bash
# Should return 401 Unauthorized without token
curl -X GET http://localhost:8080/api/v1/users

# Should work with valid token
curl -X GET http://localhost:8080/api/v1/users \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Test Admin Endpoints
```bash
# Should return 403 Forbidden for regular users
curl -X GET http://localhost:8080/api/v1/admin/users \
  -H "Authorization: Bearer USER_TOKEN"

# Should work for admin users
curl -X GET http://localhost:8080/api/v1/admin/users \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

---

## Summary

| Endpoint Type | Configuration Location | Example |
|--------------|------------------------|---------|
| Public | `SecurityConstants.PUBLIC_ENDPOINTS` | `/api/v1/auth/**` |
| Authenticated | `SecurityConstants.AUTHENTICATED_ENDPOINTS` | `/api/v1/users/**` |
| Admin Only | `SecurityConstants.ADMIN_ENDPOINTS` | `/api/v1/admin/**` |
| Development | `SecurityConstants.DEV_ENDPOINTS` | `/h2-console/**` |
| Method-Level | Controller method annotations | `@PreAuthorize("hasRole('ADMIN')")` |

**Remember**: Security is not one-size-fits-all. Choose the right approach based on your specific requirements!
