package com.expenseflow.expense.controller;

import com.expenseflow.dto.ApiResponse;
import com.expenseflow.expense.domain.valueobject.StorageProvider;
import com.expenseflow.expense.dto.*;
import com.expenseflow.expense.service.ExpenseCommandService;
import com.expenseflow.expense.service.ExpenseQueryService;
import com.expenseflow.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Expense Management", description = "Endpoints resolving shared financial transactions, split calculations, status lifecycle, and receipt attachments")
public class ExpenseController {

    private final ExpenseCommandService expenseCommandService;
    private final ExpenseQueryService expenseQueryService;

    @PostMapping("/api/v1/groups/{groupId}/expenses")
    @Operation(summary = "Create a new shared expense inside a group")
    public ResponseEntity<ApiResponse<ExpenseDto>> createExpense(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("groupId") String groupId,
            @Valid @RequestBody CreateExpenseRequest request) {
        ExpenseDto response = expenseCommandService.createExpense(groupId, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Expense created successfully", response));
    }

    @GetMapping("/api/v1/expenses")
    @Operation(summary = "Retrieve all shared expenses involving the current user (User Expense Ledger)")
    public ResponseEntity<ApiResponse<Page<ExpenseDto>>> getUserExpenses(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        Page<ExpenseDto> response = expenseQueryService.getUserExpenses(principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("User expenses retrieved successfully", response));
    }

    @GetMapping("/api/v1/groups/{groupId}/expenses")
    @Operation(summary = "Retrieve group expenses, pageable, with optional trip-specific filtering")
    public ResponseEntity<ApiResponse<Page<ExpenseDto>>> getGroupExpenses(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("groupId") String groupId,
            @RequestParam(value = "tripId", required = false) String tripId,
            Pageable pageable) {
        Page<ExpenseDto> response = expenseQueryService.getGroupExpenses(groupId, tripId, principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Group expenses retrieved successfully", response));
    }

    @GetMapping("/api/v1/expenses/{id}")
    @Operation(summary = "Retrieve detailed information of a shared expense")
    public ResponseEntity<ApiResponse<ExpenseDto>> getExpenseDetails(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        ExpenseDto response = expenseQueryService.getExpenseDetails(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Expense details retrieved successfully", response));
    }

    @PatchMapping("/api/v1/expenses/{id}")
    @Operation(summary = "Update description, category, splits, and payer details of an expense")
    public ResponseEntity<ApiResponse<ExpenseDto>> updateExpense(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateExpenseRequest request) {
        ExpenseDto response = expenseCommandService.updateExpense(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Expense updated successfully", response));
    }

    @DeleteMapping("/api/v1/expenses/{id}")
    @Operation(summary = "Soft delete a shared expense")
    public ResponseEntity<ApiResponse<Void>> deleteExpense(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        expenseCommandService.deleteExpense(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Expense deleted successfully", null));
    }

    @PostMapping("/api/v1/expenses/{id}/status")
    @Operation(summary = "Transition expense lifecycle status (DRAFT -> POSTED -> VOID)")
    public ResponseEntity<ApiResponse<ExpenseDto>> updateExpenseStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateExpenseStatusRequest request) {
        ExpenseDto response = expenseCommandService.transitionStatus(id, request.status(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Expense status updated successfully", response));
    }

    @PostMapping("/api/v1/expenses/{id}/attachments")
    @Operation(summary = "Associate an attachment metadata record with the expense")
    public ResponseEntity<ApiResponse<ExpenseDto>> addAttachment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @RequestParam("url") String url,
            @RequestParam("fileName") String fileName,
            @RequestParam("fileSize") long fileSize,
            @RequestParam("fileType") String fileType,
            @RequestParam("storageProvider") StorageProvider provider) {
        ExpenseDto response = expenseCommandService.addAttachment(id, url, fileName, fileSize, fileType, provider, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Attachment associated successfully", response));
    }

    @DeleteMapping("/api/v1/expenses/{id}/attachments/{attachmentId}")
    @Operation(summary = "De-associate and remove attachment metadata from the expense")
    public ResponseEntity<ApiResponse<ExpenseDto>> removeAttachment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @PathVariable("attachmentId") String attachmentId) {
        ExpenseDto response = expenseCommandService.removeAttachment(id, attachmentId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Attachment removed successfully", response));
    }
}
