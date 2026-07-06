package com.expenseflow.controller;

import com.expenseflow.dto.ApiResponse;
import com.expenseflow.dto.auth.*;
import com.expenseflow.dto.user.UserDto;
import com.expenseflow.service.AuthService;
import com.expenseflow.service.TokenService;
import com.expenseflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Interface", description = "Endpoints for user registration, login, logout, verification, and session recovery")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final TokenService tokenService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody RegisterRequest request) {
        UserDto user = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful. Verification email has been sent.", user));
    }

    @PostMapping("/check-email")
    @Operation(summary = "Check if an email is already registered")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmail(@Valid @RequestBody CheckEmailRequest request) {
        boolean exists = userService.existsByEmail(request.getEmail());
        // Generic response schema preventing email existence disclosure
        return ResponseEntity.ok(ApiResponse.success("Email check completed", Map.of("available", !exists)));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify user email using token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully"));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Verification email has been resent."));
    }

    @PostMapping("/login")
    @Operation(summary = "Login to user account")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request, 
            HttpServletRequest servletRequest) {
        
        LoginContext loginContext = buildLoginContext(servletRequest, request.getEmail());
        LoginResponse response = authService.login(request, loginContext);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh user access token")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(
            @Valid @RequestBody TokenRefreshRequest request, 
            HttpServletRequest servletRequest) {
        
        LoginContext loginContext = buildLoginContext(servletRequest, null);
        TokenRefreshResponse response = tokenService.rotateRefreshToken(request.getRefreshToken(), loginContext);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout of user account")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody TokenRefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset link")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("If the email is registered, a password reset link has been sent."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully."));
    }

    private LoginContext buildLoginContext(HttpServletRequest request, String email) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }

        String userAgent = request.getHeader("User-Agent");

        // Parse Browser
        String browser = "Unknown";
        if (userAgent != null) {
            if (userAgent.contains("Firefox")) browser = "Firefox";
            else if (userAgent.contains("Chrome")) browser = "Chrome";
            else if (userAgent.contains("Safari") && !userAgent.contains("Chrome")) browser = "Safari";
            else if (userAgent.contains("Edge")) browser = "Edge";
        }

        // Parse OS
        String os = "Unknown";
        if (userAgent != null) {
            if (userAgent.contains("Windows")) os = "Windows";
            else if (userAgent.contains("Macintosh") || userAgent.contains("Mac OS")) os = "macOS";
            else if (userAgent.contains("Linux")) os = "Linux";
            else if (userAgent.contains("Android")) os = "Android";
            else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) os = "iOS";
        }

        // Parse Device
        String device = "Desktop";
        if (userAgent != null) {
            if (userAgent.contains("Mobi") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
                device = "Mobile";
            }
        }

        // Trace IDs
        String reqId = request.getHeader("X-Request-Id");
        if (reqId == null || reqId.isEmpty()) reqId = UUID.randomUUID().toString();
        String corrId = request.getHeader("X-Correlation-Id");
        if (corrId == null || corrId.isEmpty()) corrId = reqId;
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) traceId = corrId;

        return LoginContext.builder()
                .email(email)
                .ipAddress(ip)
                .userAgent(userAgent)
                .browser(browser)
                .operatingSystem(os)
                .deviceName(device)
                .requestId(reqId)
                .correlationId(corrId)
                .traceId(traceId)
                .loginTime(java.time.LocalDateTime.now())
                .build();
    }
}
