package com.lilknig.emberapi.service;

import com.lilknig.emberapi.dto.response.UserResponse;
import com.lilknig.emberapi.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findByEmail(String email);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
}
