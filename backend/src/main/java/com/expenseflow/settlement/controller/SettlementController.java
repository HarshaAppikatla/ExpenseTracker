package com.expenseflow.settlement.controller;

import com.expenseflow.dto.ApiResponse;
import com.expenseflow.security.UserPrincipal;
import com.expenseflow.settlement.dto.ConfirmSettlementRequest;
import com.expenseflow.settlement.dto.DisputeSettlementRequest;
import com.expenseflow.settlement.dto.SettlementResponse;
import com.expenseflow.settlement.dto.SettlementSummaryResponse;
import com.expenseflow.settlement.service.SettlementCommandService;
import com.expenseflow.settlement.service.SettlementQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Settlement Management", description = "Endpoints for generating, disputing, and confirming group-scoped settlement payments.")
public class SettlementController {

    private final SettlementCommandService settlementCommandService;
    private final SettlementQueryService settlementQueryService;

    @PostMapping("/api/v1/groups/{groupId}/settlements/generate")
    @Operation(summary = "Generate settlements for all POSTED expenses in a group (Idempotent)")
    public ResponseEntity<ApiResponse<SettlementSummaryResponse>> generateSettlements(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("groupId") String groupId,
            @RequestParam(value = "tripId", required = false) String tripId) {
        SettlementSummaryResponse response = settlementCommandService.generateSettlements(groupId, tripId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Settlements generated successfully", response));
    }

    @GetMapping("/api/v1/groups/{groupId}/settlements")
    @Operation(summary = "Get the outstanding balance summary and pending settlements in a group")
    public ResponseEntity<ApiResponse<SettlementSummaryResponse>> getSettlementSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("groupId") String groupId) {
        SettlementSummaryResponse response = settlementQueryService.getSettlementSummary(groupId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Settlement summary retrieved successfully", response));
    }

    @GetMapping("/api/v1/groups/{groupId}/settlements/trip/{tripId}")
    @Operation(summary = "Get settlements scoped to a trip (paginated)")
    public ResponseEntity<ApiResponse<Page<SettlementResponse>>> getSettlementsByTrip(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("groupId") String groupId,
            @PathVariable("tripId") String tripId,
            Pageable pageable) {
        Page<SettlementResponse> response = settlementQueryService.getSettlementsByTrip(groupId, tripId, principal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Trip settlements retrieved successfully", response));
    }

    @GetMapping("/api/v1/groups/{groupId}/settlements/me")
    @Operation(summary = "Get settlements involving the current user in a group")
    public ResponseEntity<ApiResponse<List<SettlementResponse>>> getMySettlements(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("groupId") String groupId) {
        List<SettlementResponse> response = settlementQueryService.getMySettlements(groupId, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("User settlements retrieved successfully", response));
    }

    @GetMapping("/api/v1/settlements/{id}")
    @Operation(summary = "Retrieve details of a single settlement")
    public ResponseEntity<ApiResponse<SettlementResponse>> getSettlementById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        SettlementResponse response = settlementQueryService.getSettlementById(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Settlement retrieved successfully", response));
    }

    @PostMapping("/api/v1/settlements/{id}/confirm")
    @Operation(summary = "Debtor confirms payment of a pending/disputed settlement")
    public ResponseEntity<ApiResponse<SettlementResponse>> confirmPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody ConfirmSettlementRequest request) {
        SettlementResponse response = settlementCommandService.markAsPaid(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Settlement payment confirmed successfully", response));
    }

    @PostMapping("/api/v1/settlements/{id}/dispute")
    @Operation(summary = "Creditor disputes a pending/confirmed settlement")
    public ResponseEntity<ApiResponse<SettlementResponse>> disputeSettlement(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody DisputeSettlementRequest request) {
        SettlementResponse response = settlementCommandService.disputeSettlement(id, request.reason(), principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Settlement dispute raised successfully", response));
    }

    @PostMapping("/api/v1/settlements/{id}/resolve")
    @Operation(summary = "Group owner/admin resolves a disputed settlement and marks it confirmed")
    public ResponseEntity<ApiResponse<SettlementResponse>> resolveSettlement(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {
        SettlementResponse response = settlementCommandService.resolveSettlement(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Settlement dispute resolved and marked as settled", response));
    }
}
