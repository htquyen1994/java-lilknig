package com.lilknig.emberapi.dto.response;

import com.lilknig.emberapi.entity.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private AuthProvider provider;
    private LocalDateTime createdAt;
}
