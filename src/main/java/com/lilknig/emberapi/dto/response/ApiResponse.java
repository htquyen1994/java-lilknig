package com.lilknig.emberapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int statusCode;
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // Success responses
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, true, "Success", data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, true, message, data, LocalDateTime.now());
    }

    // Error responses
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, false, message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> badRequest(String message, T data) {
        return new ApiResponse<>(400, false, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(401, false, message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(403, false, message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, false, message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> internalError(String message) {
        return new ApiResponse<>(500, false, message, null, LocalDateTime.now());
    }

    // Generic error with custom status code
    public static <T> ApiResponse<T> error(int statusCode, String message) {
        return new ApiResponse<>(statusCode, false, message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(int statusCode, String message, T data) {
        return new ApiResponse<>(statusCode, false, message, data, LocalDateTime.now());
    }
}
