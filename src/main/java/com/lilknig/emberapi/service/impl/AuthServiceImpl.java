package com.lilknig.emberapi.service.impl;

import com.lilknig.emberapi.dto.request.LoginRequest;
import com.lilknig.emberapi.dto.request.RegisterRequest;
import com.lilknig.emberapi.dto.response.UserResponse;
import com.lilknig.emberapi.entity.AuthProvider;
import com.lilknig.emberapi.entity.User;
import com.lilknig.emberapi.exception.BadRequestException;
import com.lilknig.emberapi.exception.UnauthorizedException;
import com.lilknig.emberapi.repository.UserRepository;
import com.lilknig.emberapi.service.AuthService;
import com.lilknig.emberapi.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setName(request.getName());
        user.setProvider(AuthProvider.LOCAL);

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    @Override
    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        // Verify password
        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setProvider(user.getProvider());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
