# API Response Examples - Complete Guide

This document provides comprehensive examples of all API responses with HTTP status codes.

---

## Response Structure

Every API response follows this structure:

```json
{
  "statusCode": 200,           // HTTP status code (also in HTTP header)
  "success": true,             // true for success, false for errors
  "message": "Success message", // Human-readable message
  "data": {},                  // Response data (null for errors)
  "timestamp": "2026-01-03T10:30:00"  // When the response was generated
}
```

---

## Success Responses

### 200 OK - Successful Request

**Use case**: General successful operation (login, get data, update)

**Example: Login Success**
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response**:
```json
HTTP/1.1 200 OK
Content-Type: application/json

{
  "statusCode": 200,
  "success": true,
  "message": "Login successful",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "John Doe",
    "provider": "LOCAL",
    "createdAt": "2026-01-03T10:00:00"
  },
  "timestamp": "2026-01-03T10:30:00"
}
```

---

### 201 Created - Resource Created Successfully

**Use case**: New resource created (registration, create post, etc.)

**Example: Registration Success**
```http
POST /auth/register
Content-Type: application/json

{
  "email": "newuser@example.com",
  "password": "securepass123",
  "name": "Jane Smith"
}
```

**Response**:
```json
HTTP/1.1 201 Created
Content-Type: application/json

{
  "statusCode": 201,
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 2,
    "email": "newuser@example.com",
    "name": "Jane Smith",
    "provider": "LOCAL",
    "createdAt": "2026-01-03T10:30:00"
  },
  "timestamp": "2026-01-03T10:30:00"
}
```

---

## Client Error Responses (4xx)

### 400 Bad Request - Invalid Input

**Use case**: Validation errors, business logic violations

**Example 1: Email Already Registered**
```http
POST /auth/register
Content-Type: application/json

{
  "email": "existing@example.com",
  "password": "password123",
  "name": "John Doe"
}
```

**Response**:
```json
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "statusCode": 400,
  "success": false,
  "message": "Email already registered",
  "data": null,
  "timestamp": "2026-01-03T10:30:00"
}
```

---

**Example 2: Validation Errors**
```http
POST /auth/register
Content-Type: application/json

{
  "email": "invalid-email",
  "password": "123",
  "name": "A"
}
```

**Response**:
```json
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "statusCode": 400,
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email should be valid",
    "password": "Password must be at least 6 characters",
    "name": "Name must be between 2 and 100 characters"
  },
  "timestamp": "2026-01-03T10:30:00"
}
```

---

### 401 Unauthorized - Authentication Failed

**Use case**: Invalid credentials, missing authentication

**Example 1: Invalid Password**
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "wrongpassword"
}
```

**Response**:
```json
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "statusCode": 401,
  "success": false,
  "message": "Invalid email or password",
  "data": null,
  "timestamp": "2026-01-03T10:30:00"
}
```

---

**Example 2: User Not Found**
```http
POST /auth/login
Content-Type: application/json

{
  "email": "nonexistent@example.com",
  "password": "password123"
}
```

**Response**:
```json
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "statusCode": 401,
  "success": false,
  "message": "Invalid email or password",
  "data": null,
  "timestamp": "2026-01-03T10:30:00"
}
```

**Note**: We return the same message for both "user not found" and "wrong password" to prevent email enumeration attacks.

---

### 403 Forbidden - Access Denied

**Use case**: Authenticated but lacks permission (for future use with roles)

**Example**: Accessing Admin Resource Without Admin Role
```http
GET /admin/users
Authorization: Bearer <user_token>
```

**Response**:
```json
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "statusCode": 403,
  "success": false,
  "message": "You don't have permission to access this resource",
  "data": null,
  "timestamp": "2026-01-03T10:30:00"
}
```

---

### 404 Not Found - Resource Not Found

**Use case**: Requested resource doesn't exist

**Example**: Get User by Non-existent ID
```http
GET /users/999
```

**Response**:
```json
HTTP/1.1 404 Not Found
Content-Type: application/json

{
  "statusCode": 404,
  "success": false,
  "message": "User not found with id: 999",
  "data": null,
  "timestamp": "2026-01-03T10:30:00"
}
```

---

## Server Error Responses (5xx)

### 500 Internal Server Error - Unexpected Error

**Use case**: Database connection failed, unexpected exceptions

**Example**: Database Connection Error
```http
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe"
}
```

**Response**:
```json
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "statusCode": 500,
  "success": false,
  "message": "An unexpected error occurred: Connection to database failed",
  "data": null,
  "timestamp": "2026-01-03T10:30:00"
}
```

---

## OAuth2 Google Login Responses

### Successful OAuth2 Login

**Flow**:
1. User clicks: `http://localhost:8080/oauth2/authorization/google`
2. Redirected to Google login
3. After successful authentication, redirected to frontend with user data

**Frontend Redirect URL**:
```
http://localhost:3000/oauth2/redirect?userId=3&email=user@gmail.com&name=John%20Doe&provider=GOOGLE
```

**Parse the URL parameters**:
```javascript
const urlParams = new URLSearchParams(window.location.search);
const userId = urlParams.get('userId');        // "3"
const email = urlParams.get('email');          // "user@gmail.com"
const name = urlParams.get('name');            // "John Doe"
const provider = urlParams.get('provider');    // "GOOGLE"
```

---

### Failed OAuth2 Login

**Frontend Redirect URL**:
```
http://localhost:3000/oauth2/redirect?error=Email%20not%20found%20from%20OAuth2%20provider
```

**Parse the error**:
```javascript
const urlParams = new URLSearchParams(window.location.search);
const error = urlParams.get('error');
if (error) {
  console.error('OAuth2 Error:', error);
  // Show error to user
}
```

---

## Complete Request/Response Examples

### Test Scenario 1: Successful User Registration and Login

**Step 1: Register New User**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "alicepass123",
    "name": "Alice Johnson"
  }'
```

**Response**:
```json
{
  "statusCode": 201,
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": 5,
    "email": "alice@example.com",
    "name": "Alice Johnson",
    "provider": "LOCAL",
    "createdAt": "2026-01-03T11:00:00"
  },
  "timestamp": "2026-01-03T11:00:00"
}
```

**Step 2: Login with New Account**
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "alicepass123"
  }'
```

**Response**:
```json
{
  "statusCode": 200,
  "success": true,
  "message": "Login successful",
  "data": {
    "id": 5,
    "email": "alice@example.com",
    "name": "Alice Johnson",
    "provider": "LOCAL",
    "createdAt": "2026-01-03T11:00:00"
  },
  "timestamp": "2026-01-03T11:00:30"
}
```

---

### Test Scenario 2: Validation Errors

**Attempt Registration with Invalid Data**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "not-an-email",
    "password": "123",
    "name": ""
  }'
```

**Response**:
```json
{
  "statusCode": 400,
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email should be valid",
    "password": "Password must be at least 6 characters",
    "name": "Name is required"
  },
  "timestamp": "2026-01-03T11:05:00"
}
```

---

### Test Scenario 3: Duplicate Email Registration

**Step 1: Register First User**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "bob@example.com",
    "password": "bobpass123",
    "name": "Bob Smith"
  }'
```

**Response**: ✅ Success (201 Created)

**Step 2: Try to Register with Same Email**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "bob@example.com",
    "password": "differentpass",
    "name": "Another Bob"
  }'
```

**Response**:
```json
{
  "statusCode": 400,
  "success": false,
  "message": "Email already registered",
  "data": null,
  "timestamp": "2026-01-03T11:10:00"
}
```

---

### Test Scenario 4: Login with Wrong Password

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "bob@example.com",
    "password": "wrongpassword"
  }'
```

**Response**:
```json
{
  "statusCode": 401,
  "success": false,
  "message": "Invalid email or password",
  "data": null,
  "timestamp": "2026-01-03T11:15:00"
}
```

---

## Frontend Integration Examples

### React/JavaScript Example

```javascript
// Register function
async function register(email, password, name) {
  try {
    const response = await fetch('http://localhost:8080/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password, name })
    });

    const data = await response.json();

    if (data.success) {
      console.log('Registration successful!');
      console.log('Status Code:', data.statusCode);  // 201
      console.log('User ID:', data.data.id);
      console.log('User Email:', data.data.email);
      // Redirect to login or dashboard
    } else {
      console.error('Registration failed!');
      console.log('Status Code:', data.statusCode);  // 400, 401, etc.
      console.log('Error Message:', data.message);

      // Handle validation errors
      if (data.data && typeof data.data === 'object') {
        Object.keys(data.data).forEach(field => {
          console.log(`${field}: ${data.data[field]}`);
        });
      }
    }
  } catch (error) {
    console.error('Network error:', error);
  }
}

// Login function
async function login(email, password) {
  try {
    const response = await fetch('http://localhost:8080/auth/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password })
    });

    const data = await response.json();

    if (data.success && data.statusCode === 200) {
      console.log('Login successful!');
      localStorage.setItem('user', JSON.stringify(data.data));
      // Redirect to dashboard
    } else if (data.statusCode === 401) {
      console.error('Invalid credentials');
      alert(data.message);
    } else {
      console.error('Login failed:', data.message);
    }
  } catch (error) {
    console.error('Network error:', error);
  }
}

// Usage
register('user@example.com', 'password123', 'John Doe');
login('user@example.com', 'password123');
```

---

### Handling Different Status Codes in Frontend

```javascript
async function apiCall(endpoint, method, body) {
  try {
    const response = await fetch(`http://localhost:8080${endpoint}`, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });

    const data = await response.json();

    // Check status code in response body
    switch (data.statusCode) {
      case 200:
        console.log('Success:', data.message);
        return data.data;

      case 201:
        console.log('Created:', data.message);
        return data.data;

      case 400:
        console.error('Bad Request:', data.message);
        if (data.data) {
          // Validation errors
          showValidationErrors(data.data);
        }
        break;

      case 401:
        console.error('Unauthorized:', data.message);
        // Redirect to login
        window.location.href = '/login';
        break;

      case 403:
        console.error('Forbidden:', data.message);
        alert('You do not have permission to perform this action');
        break;

      case 404:
        console.error('Not Found:', data.message);
        alert('Resource not found');
        break;

      case 500:
        console.error('Server Error:', data.message);
        alert('An unexpected error occurred. Please try again later.');
        break;

      default:
        console.error('Unknown error:', data.message);
    }

    return null;
  } catch (error) {
    console.error('Network error:', error);
    alert('Cannot connect to server');
    return null;
  }
}

function showValidationErrors(errors) {
  Object.keys(errors).forEach(field => {
    const errorElement = document.getElementById(`${field}-error`);
    if (errorElement) {
      errorElement.textContent = errors[field];
      errorElement.style.display = 'block';
    }
  });
}
```

---

## Status Code Quick Reference

| Code | Name | When to Use | Response Method |
|------|------|-------------|-----------------|
| 200 | OK | Successful operation | `ApiResponse.success()` |
| 201 | Created | Resource created | `ApiResponse.created()` |
| 400 | Bad Request | Validation error, business rule violation | `ApiResponse.badRequest()` |
| 401 | Unauthorized | Authentication failed | `ApiResponse.unauthorized()` |
| 403 | Forbidden | Authenticated but no permission | `ApiResponse.forbidden()` |
| 404 | Not Found | Resource doesn't exist | `ApiResponse.notFound()` |
| 500 | Internal Server Error | Unexpected server error | `ApiResponse.internalError()` |

---

## Testing with Postman

### Set up Postman Collection

**1. Create New Request: Register**
- Method: `POST`
- URL: `http://localhost:8080/auth/register`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
  ```json
  {
    "email": "test@example.com",
    "password": "testpass123",
    "name": "Test User"
  }
  ```

**2. Create New Request: Login**
- Method: `POST`
- URL: `http://localhost:8080/auth/login`
- Headers: `Content-Type: application/json`
- Body (raw JSON):
  ```json
  {
    "email": "test@example.com",
    "password": "testpass123"
  }
  ```

**3. Create Test Scripts**
```javascript
// In Postman Tests tab
pm.test("Status code is 200 or 201", function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 201]);
});

pm.test("Response has statusCode field", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property('statusCode');
    pm.expect(jsonData.statusCode).to.be.oneOf([200, 201]);
});

pm.test("Response is successful", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.success).to.eql(true);
});

pm.test("Response has user data", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data).to.have.property('id');
    pm.expect(jsonData.data).to.have.property('email');
    pm.expect(jsonData.data).to.not.have.property('password');
});
```

---

## Understanding Status Codes

### Why Different Status Codes?

**HTTP status codes** communicate the result of a request:
- **2xx (Success)**: Request was successful
- **4xx (Client Error)**: Client made a mistake
- **5xx (Server Error)**: Server encountered an error

### Why Include in Response Body?

Including `statusCode` in the response body provides:
1. **Consistency**: Same structure for all responses
2. **Debugging**: Easy to see status in logs without checking headers
3. **Frontend Clarity**: No need to parse HTTP headers
4. **Testing**: Easier to assert in tests

### Example: Why 201 vs 200?

**200 (OK)**: "Here's the data you requested"
```json
{
  "statusCode": 200,
  "message": "Login successful",
  "data": { "id": 1, "email": "user@example.com" }
}
```

**201 (Created)**: "I created something new for you"
```json
{
  "statusCode": 201,
  "message": "User registered successfully",
  "data": { "id": 2, "email": "newuser@example.com" }
}
```

This helps clients understand whether they:
- Retrieved existing data (200)
- Created new data (201)

---

## Common Mistakes to Avoid

### ❌ Mistake 1: Returning 200 for Errors
```json
// WRONG - Don't do this!
{
  "statusCode": 200,
  "success": false,
  "message": "Email already registered"
}
```

**Why it's wrong**: Status code 200 means success, but success is false. This is contradictory.

✅ **Correct**:
```json
{
  "statusCode": 400,
  "success": false,
  "message": "Email already registered"
}
```

---

### ❌ Mistake 2: Returning Sensitive Data in Error
```json
// WRONG - Don't expose user object in login error!
{
  "statusCode": 401,
  "message": "Invalid password",
  "data": {
    "id": 1,
    "email": "user@example.com",
    "password": "$2a$10$..."
  }
}
```

✅ **Correct**:
```json
{
  "statusCode": 401,
  "message": "Invalid email or password",
  "data": null
}
```

---

### ❌ Mistake 3: Too Specific Error Messages
```json
// WRONG - Helps attackers enumerate emails
{
  "statusCode": 401,
  "message": "User with email user@example.com not found"
}
```

✅ **Correct**:
```json
{
  "statusCode": 401,
  "message": "Invalid email or password"
}
```

---

## Summary

✅ **Always include**:
- `statusCode`: Numeric HTTP status
- `success`: Boolean indicating success/failure
- `message`: Human-readable message
- `data`: Response data or null
- `timestamp`: When response was generated

✅ **Use appropriate status codes**:
- 200: Successful retrieval/operation
- 201: Successfully created resource
- 400: Client error (validation, business rule)
- 401: Authentication failed
- 404: Resource not found
- 500: Server error

✅ **Consistent format**:
- All responses use `ApiResponse<T>` structure
- Frontend can rely on same structure
- Easy to parse and handle errors

---

**Happy coding! 🚀**
