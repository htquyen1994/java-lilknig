package com.lilknig.emberapi.service;

import com.lilknig.emberapi.dto.request.LoginRequest;
import com.lilknig.emberapi.dto.request.RegisterRequest;
import com.lilknig.emberapi.dto.response.UserResponse;

public interface AuthService {
    UserResponse register(RegisterRequest request);
    UserResponse login(LoginRequest request);
}
