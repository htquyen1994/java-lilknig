package com.lilknig.emberapi.constant;

public final class SecurityConstants {

    // API Version Paths
    public static final String API_V1 = "/api/v1";

    // Public Endpoints (No Authentication Required)
    public static final String[] PUBLIC_ENDPOINTS = {
            API_V1 + "/auth/**",           // Authentication endpoints (login, register)
            "/oauth2/**",                  // OAuth2 endpoints
            "/login/oauth2/**",            // OAuth2 login callback
            "/error",                      // Error page
            "/actuator/health",             // Health check endpoint
            API_V1 + "/users/**",
    };

    // Development Only Endpoints
    public static final String[] DEV_ENDPOINTS = {
            "/h2-console/**",              // H2 database console
            "/swagger-ui/**",              // Swagger UI
            "/v3/api-docs/**",             // OpenAPI docs
            "/actuator/**"                 // Spring Boot Actuator
    };

    // Protected Endpoints - Require Authentication
    public static final String[] AUTHENTICATED_ENDPOINTS = {
            API_V1 + "/users/**",          // User management
            API_V1 + "/profile/**"         // User profile
    };

    // Admin Only Endpoints
    public static final String[] ADMIN_ENDPOINTS = {
            API_V1 + "/admin/**"           // Admin panel
    };

    // Roles
    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    private SecurityConstants() {
        // Prevent instantiation
    }
}
