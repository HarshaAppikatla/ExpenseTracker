package com.expenseflow.trip.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "trip_activities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripActivityEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", length = 50, nullable = false)
    private com.expenseflow.trip.domain.valueobject.TripActivityType activityType;

    @Column(name = "actor_user_id", length = 36, nullable = false)
    private String actorUserId;

    @Column(name = "target_user_id", length = 36)
    private String targetUserId;

    @Column(name = "message", length = 255, nullable = false)
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata_json")
    private Map<String, Object> metadataJson;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
