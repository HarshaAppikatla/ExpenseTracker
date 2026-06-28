package com.expenseflow.controller;

import com.expenseflow.constants.AppConstants;
import com.expenseflow.dto.ApiResponse;
import com.expenseflow.dto.HealthDetails;
import com.expenseflow.dto.InfoDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping(AppConstants.API_BASE_PATH)
@Tag(name = "Health & Info Interface", description = "Endpoints for verifying server availability and system environment properties.")
public class HealthController {

    @Value("${spring.application.name:ExpenseFlow}")
    private String appName;

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @GetMapping("/health")
    @Operation(summary = "Check Backend Health Status", description = "Returns active profile environment name, version, status, and request timestamp.")
    public ResponseEntity<ApiResponse<HealthDetails>> checkHealth() {
        HealthDetails details = HealthDetails.builder()
                .name(appName)
                .version("1.0.0")
                .environment(activeProfile)
                .timestamp(Instant.now().toString())
                .status(AppConstants.HEALTH_OK)
                .build();
                
        return ResponseEntity.ok(ApiResponse.success("ExpenseFlow Backend Running", details));
    }

    @GetMapping("/info")
    @Operation(summary = "Get Application Info", description = "Returns framework versions, system uptime, and active profile environment.")
    public ResponseEntity<ApiResponse<InfoDetails>> getInfo() {
        long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long uptimeSecs = uptimeMs / 1000;
        String uptimeFormatted = String.format("%dh %dm %ds", 
                uptimeSecs / 3600, 
                (uptimeSecs % 3600) / 60, 
                uptimeSecs % 60
        );

        InfoDetails details = InfoDetails.builder()
                .application(appName)
                .version("0.1.0")
                .environment(activeProfile)
                .javaVersion(System.getProperty("java.version"))
                .springBootVersion(org.springframework.boot.SpringBootVersion.getVersion())
                .buildTime("2026-06-28T11:20:00Z")
                .uptime(uptimeFormatted)
                .build();
                
        return ResponseEntity.ok(ApiResponse.success("ExpenseFlow Application Info Retrieved", details));
    }
}
