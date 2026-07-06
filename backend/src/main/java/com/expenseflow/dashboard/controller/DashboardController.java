package com.expenseflow.dashboard.controller;

import com.expenseflow.dashboard.dto.DashboardResponse;
import com.expenseflow.dashboard.service.DashboardService;
import com.expenseflow.dto.ApiResponse;
import com.expenseflow.insight.dto.InsightDashboardDto;
import com.expenseflow.insight.service.InsightService;
import com.expenseflow.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final InsightService insightService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardSummary(
            @AuthenticationPrincipal UserPrincipal principal) {

        DashboardResponse summary = dashboardService.getDashboardSummary(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved successfully", summary));
    }

    @GetMapping("/financial")
    @Cacheable(value = "financialDashboard", key = "#principal.id")
    public ResponseEntity<ApiResponse<InsightDashboardDto>> getFinancialDashboard(
            @AuthenticationPrincipal UserPrincipal principal) {

        InsightDashboardDto insights = insightService.getDashboardInsights(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Financial dashboard retrieved successfully", insights));
    }
}

