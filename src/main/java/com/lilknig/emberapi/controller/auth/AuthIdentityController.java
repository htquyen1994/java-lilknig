package com.lilknig.emberapi.controller.auth;

import com.lilknig.emberapi.dto.request.LoginRequest;
import com.lilknig.emberapi.dto.request.RegisterRequest;
import com.lilknig.emberapi.dto.response.ApiResponse;
import com.lilknig.emberapi.dto.response.UserResponse;
import com.lilknig.emberapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthIdentityController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = userService.register(request);
        ApiResponse<UserResponse> response = ApiResponse.success("User registered successfully", userResponse);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        UserResponse userResponse = userService.login(request);
        ApiResponse<UserResponse> response = ApiResponse.success("Login successful", userResponse);
        return ResponseEntity.ok(response);
    }
}
