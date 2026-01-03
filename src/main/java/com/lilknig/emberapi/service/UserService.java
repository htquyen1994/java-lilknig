package com.lilknig.emberapi.service;

import com.lilknig.emberapi.dto.request.LoginRequest;
import com.lilknig.emberapi.dto.request.RegisterRequest;
import com.lilknig.emberapi.dto.response.UserResponse;
import com.lilknig.emberapi.entity.User;

import java.util.Optional;

public interface UserService {
    UserResponse register(RegisterRequest request);
    UserResponse login(LoginRequest request);
    Optional<User> findByEmail(String email);
}
