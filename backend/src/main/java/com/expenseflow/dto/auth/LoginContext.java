package com.expenseflow.dto.auth;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginContext {
    private String email;
    private String ipAddress;
    private String userAgent;
    private String browser;
    private String operatingSystem;
    private String deviceName;
    private String requestId;
    private String correlationId;
    private String traceId;
    private LocalDateTime loginTime;
}
