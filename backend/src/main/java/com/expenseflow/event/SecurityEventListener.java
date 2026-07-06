package com.expenseflow.event;

import com.expenseflow.entity.SecurityEventEntity;
import com.expenseflow.entity.SecurityEventSeverity;
import com.expenseflow.event.authentication.*;
import com.expenseflow.event.security.*;
import com.expenseflow.repository.SecurityEventRepository;
import com.expenseflow.util.MetricNames;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityEventListener {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventListener.class);

    private final SecurityEventRepository securityEventRepository;
    private final MeterRegistry meterRegistry;

    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("User registered: {}", event.getUser().getEmail());
        meterRegistry.counter("auth.registration").increment();
        
        SecurityEventEntity securityEvent = SecurityEventEntity.builder()
                .user(event.getUser())
                .eventType("REGISTRATION")
                .severity(SecurityEventSeverity.INFO)
                .build();
        securityEventRepository.save(securityEvent);
    }

    @EventListener
    public void handleLoginSuccess(LoginSuccessEvent event) {
        log.info("Login success for user: {}", event.getUser().getEmail());
        meterRegistry.counter(MetricNames.LOGIN_SUCCESS).increment();

        SecurityEventEntity securityEvent = SecurityEventEntity.builder()
                .user(event.getUser())
                .eventType("LOGIN_SUCCESS")
                .severity(SecurityEventSeverity.INFO)
                .ipAddress(event.getLoginContext().getIpAddress())
                .userAgent(event.getLoginContext().getUserAgent())
                .requestId(event.getLoginContext().getRequestId())
                .correlationId(event.getLoginContext().getCorrelationId())
                .traceId(event.getLoginContext().getTraceId())
                .build();
        securityEventRepository.save(securityEvent);
    }

    @EventListener
    public void handleLoginFailure(LoginFailureEvent event) {
        log.warn("Login failure for email: {}. Reason: {}", event.getEmail(), event.getReason());
        meterRegistry.counter(MetricNames.LOGIN_FAILURE).increment();

        SecurityEventEntity securityEvent = SecurityEventEntity.builder()
                .eventType("LOGIN_FAILURE")
                .severity(SecurityEventSeverity.WARNING)
                .ipAddress(event.getLoginContext().getIpAddress())
                .userAgent(event.getLoginContext().getUserAgent())
                .requestId(event.getLoginContext().getRequestId())
                .correlationId(event.getLoginContext().getCorrelationId())
                .traceId(event.getLoginContext().getTraceId())
                .build();
        securityEventRepository.save(securityEvent);
    }

    @EventListener
    public void handleAccountLocked(AccountLockedEvent event) {
        log.warn("Account locked for user: {} for {} minutes", event.getUser().getEmail(), event.getLockDurationMinutes());
        meterRegistry.counter(MetricNames.ACCOUNT_LOCKED).increment();

        SecurityEventEntity securityEvent = SecurityEventEntity.builder()
                .user(event.getUser())
                .eventType("ACCOUNT_LOCKED")
                .severity(SecurityEventSeverity.WARNING)
                .ipAddress(event.getLoginContext().getIpAddress())
                .userAgent(event.getLoginContext().getUserAgent())
                .requestId(event.getLoginContext().getRequestId())
                .correlationId(event.getLoginContext().getCorrelationId())
                .traceId(event.getLoginContext().getTraceId())
                .build();
        securityEventRepository.save(securityEvent);
    }

    @EventListener
    public void handleReplayDetected(ReplayDetectedEvent event) {
        log.error("Token replay detected for user ID: {} in family: {}", event.getUserId(), event.getTokenFamily());
        meterRegistry.counter(MetricNames.REFRESH_REPLAY).increment();

        SecurityEventEntity securityEvent = SecurityEventEntity.builder()
                .eventType("TOKEN_REPLAY_DETECTED")
                .severity(SecurityEventSeverity.CRITICAL)
                .ipAddress(event.getLoginContext().getIpAddress())
                .userAgent(event.getLoginContext().getUserAgent())
                .requestId(event.getLoginContext().getRequestId())
                .correlationId(event.getLoginContext().getCorrelationId())
                .traceId(event.getLoginContext().getTraceId())
                .build();
        securityEventRepository.save(securityEvent);
    }

    @EventListener
    public void handleRateLimitExceeded(RateLimitExceededEvent event) {
        log.warn("Rate limit exceeded for IP: {} on endpoint: {}", event.getIpAddress(), event.getEndpointType());
        meterRegistry.counter(MetricNames.RATE_LIMIT_EXCEEDED).increment();

        SecurityEventEntity securityEvent = SecurityEventEntity.builder()
                .eventType("RATE_LIMIT_EXCEEDED")
                .severity(SecurityEventSeverity.WARNING)
                .ipAddress(event.getIpAddress())
                .build();
        securityEventRepository.save(securityEvent);
    }
}
