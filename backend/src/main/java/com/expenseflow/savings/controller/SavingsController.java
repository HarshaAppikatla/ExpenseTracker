package com.expenseflow.savings.controller;

import com.expenseflow.dto.ApiResponse;
import com.expenseflow.savings.dto.SavingsDepositDto;
import com.expenseflow.savings.dto.SavingsDepositRequest;
import com.expenseflow.savings.dto.SavingsGoalDto;
import com.expenseflow.savings.dto.SavingsGoalRequest;
import com.expenseflow.savings.service.SavingsService;
import com.expenseflow.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/savings")
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsService savingsService;

    @PostMapping
    public ResponseEntity<ApiResponse<SavingsGoalDto>> createGoal(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody SavingsGoalRequest request) {
        SavingsGoalDto response = savingsService.createGoal(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Savings goal created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SavingsGoalDto>> updateGoal(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody SavingsGoalRequest request) {
        SavingsGoalDto response = savingsService.updateGoal(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Savings goal updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        savingsService.deleteGoal(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Savings goal deleted successfully", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SavingsGoalDto>>> getGoals(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<SavingsGoalDto> response = savingsService.getGoals(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Savings goals retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SavingsGoalDto>> getGoalDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        SavingsGoalDto response = savingsService.getGoalDetails(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Savings goal details retrieved successfully", response));
    }

    @PostMapping("/{id}/deposits")
    public ResponseEntity<ApiResponse<SavingsDepositDto>> addDeposit(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody SavingsDepositRequest request) {
        SavingsDepositDto response = savingsService.addDeposit(principal.getId(), id, request, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Savings deposit added successfully", response));
    }

    @GetMapping("/{id}/deposits")
    public ResponseEntity<ApiResponse<List<SavingsDepositDto>>> getDeposits(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        List<SavingsDepositDto> response = savingsService.getDeposits(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Savings deposits retrieved successfully", response));
    }
}
