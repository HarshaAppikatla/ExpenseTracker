package com.expenseflow.profile.controller;

import com.expenseflow.dto.ApiResponse;
import com.expenseflow.profile.dto.OnboardingRequest;
import com.expenseflow.profile.dto.UserProfileResponse;
import com.expenseflow.profile.service.UserProfileService;
import com.expenseflow.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        UserProfileResponse response = userProfileService.getProfile(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("User profile resolved", response));
    }

    @PostMapping("/onboarding")
    public ResponseEntity<ApiResponse<UserProfileResponse>> onboardUser(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody OnboardingRequest request) {
        
        UserProfileResponse response = userProfileService.onboardUser(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Onboarding completed successfully", response));
    }


    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody OnboardingRequest request) {
        
        UserProfileResponse response = userProfileService.updateProfile(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", response));
    }
}
