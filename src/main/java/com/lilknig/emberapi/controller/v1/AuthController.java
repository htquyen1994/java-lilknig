package com.lilknig.emberapi.controller.v1;

import com.lilknig.emberapi.dto.request.LoginRequest;
import com.lilknig.emberapi.dto.request.RegisterRequest;
import com.lilknig.emberapi.dto.response.ApiResponse;
import com.lilknig.emberapi.dto.response.UserResponse;
import com.lilknig.emberapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        ApiResponse<UserResponse> response = ApiResponse.success("User registered successfully", user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        UserResponse user = authService.login(request);
        ApiResponse<UserResponse> response = ApiResponse.success("Login successful", user);
        return ResponseEntity.ok(response);
    }
}
