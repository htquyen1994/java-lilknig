package com.lilknig.emberapi.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static String encode(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public static boolean isStrongPassword(String password) {
        // Password strength validation: at least 6 characters
        // Can be enhanced with more rules (uppercase, lowercase, digits, special chars)
        return password != null && password.length() >= 6;
    }
}
