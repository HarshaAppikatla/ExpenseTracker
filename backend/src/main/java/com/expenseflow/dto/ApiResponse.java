package com.expenseflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private String code;
    private T data;
    private String timestamp;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(message, null);
    }

    public static <T> ApiResponse<T> error(String message, String code) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code(code)
                .timestamp(Instant.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String code, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code(code)
                .data(data)
                .timestamp(Instant.now().toString())
                .build();
    }
}
