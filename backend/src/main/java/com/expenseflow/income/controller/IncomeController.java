package com.expenseflow.income.controller;

import com.expenseflow.dto.ApiResponse;
import com.expenseflow.income.dto.IncomeRequest;
import com.expenseflow.income.dto.IncomeResponse;
import com.expenseflow.income.service.IncomeService;
import com.expenseflow.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/income")
@RequiredArgsConstructor
public class IncomeController {

    private final IncomeService incomeService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<IncomeResponse>>> getIncomeList(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "incomeDate,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort sortOrder = Sort.by(sortParams[0]);
        if (sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])) {
            sortOrder = sortOrder.descending();
        }

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<IncomeResponse> response = incomeService.getIncomeList(principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Income list retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<IncomeResponse>> getIncomeById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {

        IncomeResponse response = incomeService.getIncomeById(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Income resolved successfully", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<IncomeResponse>> createIncome(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody IncomeRequest request) {

        IncomeResponse response = incomeService.createIncome(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Income recorded successfully", response));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<IncomeResponse>> updateIncome(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody IncomeRequest request) {

        IncomeResponse response = incomeService.updateIncome(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Income updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteIncome(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {

        incomeService.deleteIncome(principal.getId(), id, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Income deleted successfully"));
    }
}
