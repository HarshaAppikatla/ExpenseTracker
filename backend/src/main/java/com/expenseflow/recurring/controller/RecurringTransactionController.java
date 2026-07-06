package com.expenseflow.recurring.controller;

import com.expenseflow.dto.ApiResponse;
import com.expenseflow.recurring.dto.RecurringDto;
import com.expenseflow.recurring.dto.RecurringRequest;
import com.expenseflow.recurring.service.RecurringTransactionService;
import com.expenseflow.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recurring")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<RecurringDto>> createRecurring(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RecurringRequest request) {
        RecurringDto response = recurringTransactionService.createRecurring(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction template created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RecurringDto>> updateRecurring(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody RecurringRequest request) {
        RecurringDto response = recurringTransactionService.updateRecurring(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction template updated successfully", response));
    }

    @PutMapping("/{id}/pause")
    public ResponseEntity<ApiResponse<Void>> pauseRecurring(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        recurringTransactionService.pauseRecurring(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction template paused successfully", null));
    }

    @PutMapping("/{id}/resume")
    public ResponseEntity<ApiResponse<Void>> resumeRecurring(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        recurringTransactionService.resumeRecurring(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction template resumed successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRecurring(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        recurringTransactionService.deleteRecurring(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Recurring transaction template deleted successfully", null));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RecurringDto>>> getRecurrings(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<RecurringDto> response = recurringTransactionService.getRecurrings(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Recurring templates list retrieved successfully", response));
    }
}
