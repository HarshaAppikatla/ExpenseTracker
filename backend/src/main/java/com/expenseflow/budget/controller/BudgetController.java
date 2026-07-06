package com.expenseflow.budget.controller;

import com.expenseflow.dto.ApiResponse;
import com.expenseflow.budget.dto.BudgetDto;
import com.expenseflow.budget.dto.BudgetProgressDto;
import com.expenseflow.budget.dto.BudgetRequest;
import com.expenseflow.budget.service.BudgetService;
import com.expenseflow.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<ApiResponse<BudgetDto>> createBudget(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BudgetRequest request) {
        BudgetDto response = budgetService.createBudget(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Budget created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<BudgetDto>> updateBudget(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody BudgetRequest request) {
        BudgetDto response = budgetService.updateBudget(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Budget updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBudget(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        budgetService.deleteBudget(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Budget deleted successfully", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BudgetProgressDto>>> getBudgets(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        List<BudgetProgressDto> response = budgetService.getBudgets(principal.getId(), year, month);
        return ResponseEntity.ok(ApiResponse.success("Budgets progress list retrieved successfully", response));
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<BudgetProgressDto>> getBudgetProgress(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        BudgetProgressDto response = budgetService.getBudgetProgress(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Budget progress details retrieved successfully", response));
    }
}
