package com.lilknig.emberapi.config;

import com.lilknig.emberapi.constant.SecurityConstants;
import com.lilknig.emberapi.security.oauth2.CustomOAuth2UserService;
import com.lilknig.emberapi.security.oauth2.OAuth2AuthenticationFailureHandler;
import com.lilknig.emberapi.security.oauth2.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

/**
 * Security Configuration - Best Practices
 *
 * Security Strategy:
 * 1. Deny by default - All endpoints require authentication unless explicitly permitted
 * 2. Public endpoints - Authentication/OAuth2 flows must be accessible to everyone
 * 3. Authenticated endpoints - User must be logged in
 * 4. Role-based endpoints - User must have specific roles (ADMIN, USER, etc.)
 * 5. Development endpoints - Only enabled in dev/local profiles
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true) // Enable method-level security
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final Environment environment;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless REST API (use CSRF for session-based apps)
                .csrf(AbstractHttpConfigurer::disable)

                // Configure CORS (ensure CorsConfig is properly set up)
                .cors(cors -> cors.configure(http))

                // Stateless session - no server-side sessions (suitable for JWT/token-based auth)
                .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authorization rules - ORDER MATTERS! Most specific rules first
                .authorizeHttpRequests(auth -> {
                    // 1. Public endpoints - no authentication required
                    auth.requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS).permitAll();

                    // 2. Development-only endpoints (H2, Swagger, Actuator)
                    if (isDevelopmentEnvironment()) {
                        auth.requestMatchers(SecurityConstants.DEV_ENDPOINTS).permitAll();
                    }

                    // 3. Admin-only endpoints - require ADMIN role
                    auth.requestMatchers(SecurityConstants.ADMIN_ENDPOINTS)
                        .hasRole(SecurityConstants.ROLE_ADMIN);

                    // 4. Protected endpoints - require authentication
                    auth.requestMatchers(SecurityConstants.AUTHENTICATED_ENDPOINTS)
                        .authenticated();

                    // 5. Deny all other requests by default (security by default)
                    auth.anyRequest().authenticated();
                })

                // OAuth2 login configuration
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )

                // Allow H2 console frames (only needed for H2 in development)
                .headers(headers -> headers
                    .frameOptions(frame -> frame.sameOrigin()) // More secure than disable
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Check if running in development environment
     */
    private boolean isDevelopmentEnvironment() {
        return Arrays.asList(environment.getActiveProfiles())
                .stream()
                .anyMatch(profile ->
                    profile.equalsIgnoreCase("dev") ||
                    profile.equalsIgnoreCase("local")
                );
    }
}
