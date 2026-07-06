package com.expenseflow.controller;

import com.expenseflow.dto.ApiResponse;
import com.expenseflow.dto.user.UserDto;
import com.expenseflow.security.UserPrincipal;
import com.expenseflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Directory", description = "Endpoints resolving user info and settings")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user profile details")
    public ResponseEntity<ApiResponse<UserDto>> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        UserDto profile = userService.getCurrentUserProfile(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Authenticated user profile resolved", profile));
    }
}
