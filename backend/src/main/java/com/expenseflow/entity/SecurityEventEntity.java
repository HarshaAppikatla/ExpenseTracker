package com.expenseflow.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "security_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityEventEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(name = "event_type", length = 50, nullable = false)
    private String eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20, nullable = false)
    @Builder.Default
    private SecurityEventSeverity severity = SecurityEventSeverity.INFO;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "request_id", length = 50)
    private String requestId;

    @Column(name = "correlation_id", length = 50)
    private String correlationId;

    @Column(name = "trace_id", length = 50)
    private String traceId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }
}
