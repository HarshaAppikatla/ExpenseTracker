package com.expenseflow.trip.domain.entity;

import com.expenseflow.entity.BaseEntity;
import com.expenseflow.trip.domain.valueobject.TripParticipantStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_participants", uniqueConstraints = {
    @UniqueConstraint(name = "uq_trip_participants_trip_user", columnNames = {"trip_id", "user_id"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripParticipantEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private TripEntity trip;

    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private TripParticipantStatus status = TripParticipantStatus.ACCEPTED;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Version
    @Builder.Default
    @Column(name = "version", nullable = false)
    private Long version = 0L;
}
