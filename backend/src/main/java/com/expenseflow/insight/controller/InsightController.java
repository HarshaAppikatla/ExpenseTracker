package com.expenseflow.insight.controller;

import com.expenseflow.dto.ApiResponse;
import com.expenseflow.insight.dto.InsightDashboardDto;
import com.expenseflow.insight.service.InsightService;
import com.expenseflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    @GetMapping
    public ResponseEntity<ApiResponse<InsightDashboardDto>> getDashboardInsights(
            @AuthenticationPrincipal UserPrincipal principal) {
        InsightDashboardDto insights = insightService.getDashboardInsights(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Dashboard insights retrieved successfully", insights));
    }
}
