package com.expenseflow.trip.domain.entity;

import com.expenseflow.entity.BaseEntity;
import com.expenseflow.trip.domain.valueobject.*;
import com.expenseflow.trip.exception.InvalidTripStateException;
import com.expenseflow.trip.exception.ParticipantConflictException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "trips")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Filter(name = "deletedFilter")
public class TripEntity extends BaseEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "group_id", length = 36, nullable = false)
    private String groupId;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "description", length = 255)
    private String description;

    @Embedded
    private Destination destination;

    @Builder.Default
    @Column(name = "cover_type", length = 20, nullable = false)
    private String coverType = "PRESET";

    @Column(name = "cover_image", length = 255)
    private String coverImage;

    @Embedded
    private TripSchedule schedule;

    @Column(name = "organizer_id", length = 36, nullable = false)
    private String organizerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private TripStatus status = TripStatus.PLANNING;

    @Embedded
    @Builder.Default
    private TripSettings settings = new TripSettings();

    @Version
    @Builder.Default
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TripParticipantEntity> participants = new ArrayList<>();

    // Business Methods & Invariants

    public void updateMetadata(String title, String description, Destination destination, TripSchedule schedule, TripSettings settings, String coverType, String coverImage) {
        checkMutable();
        if (schedule != null) {
            schedule.validate();
        }
        this.title = title;
        this.description = description;
        this.destination = destination;
        this.schedule = schedule;
        if (settings != null) {
            this.settings = settings;
        }
        if (coverType != null) {
            this.coverType = coverType;
        }
        this.coverImage = coverImage;
    }

    public void transitionTo(TripStatus newStatus) {
        if (this.status == newStatus) {
            return;
        }
        checkMutable();

        boolean allowed = false;
        if (this.status == TripStatus.PLANNING) {
            allowed = (newStatus == TripStatus.ACTIVE || newStatus == TripStatus.CANCELLED);
        } else if (this.status == TripStatus.ACTIVE) {
            allowed = (newStatus == TripStatus.COMPLETED);
        }

        if (!allowed) {
            throw new InvalidTripStateException("Cannot transition trip status from " + this.status + " to " + newStatus);
        }

        this.status = newStatus;
    }

    public void inviteParticipant(String userId) {
        checkMutable();
        Optional<TripParticipantEntity> existing = findParticipant(userId);
        if (existing.isPresent()) {
            TripParticipantEntity p = existing.get();
            if (p.getStatus() == TripParticipantStatus.INVITED || p.getStatus() == TripParticipantStatus.ACCEPTED) {
                throw new ParticipantConflictException("User is already invited or a participant in this trip");
            }
            // Re-invite
            p.setStatus(TripParticipantStatus.INVITED);
            p.setLeftAt(null);
            p.setJoinedAt(null);
        } else {
            TripParticipantEntity participant = TripParticipantEntity.builder()
                    .id(UUID.randomUUID().toString())
                    .trip(this)
                    .userId(userId)
                    .status(TripParticipantStatus.INVITED)
                    .build();
            this.participants.add(participant);
        }
    }

    public void acceptInvite(String userId) {
        checkMutable();
        TripParticipantEntity p = findParticipant(userId)
                .orElseThrow(() -> new InvalidTripStateException("User is not invited to this trip"));
        if (p.getStatus() != TripParticipantStatus.INVITED) {
            throw new InvalidTripStateException("User is not in INVITED state, currently: " + p.getStatus());
        }
        p.setStatus(TripParticipantStatus.ACCEPTED);
        p.setJoinedAt(LocalDateTime.now());
    }

    public void declineInvite(String userId) {
        checkMutable();
        TripParticipantEntity p = findParticipant(userId)
                .orElseThrow(() -> new InvalidTripStateException("User is not invited to this trip"));
        if (p.getStatus() != TripParticipantStatus.INVITED) {
            throw new InvalidTripStateException("User is not in INVITED state, currently: " + p.getStatus());
        }
        p.setStatus(TripParticipantStatus.DECLINED);
    }

    public void leave(String userId) {
        checkMutable();
        if (userId.equals(this.organizerId)) {
            throw new InvalidTripStateException("The organizer cannot leave the trip");
        }
        TripParticipantEntity p = findParticipant(userId)
                .orElseThrow(() -> new InvalidTripStateException("User is not a participant in this trip"));
        
        if (p.getStatus() == TripParticipantStatus.LEFT || p.getStatus() == TripParticipantStatus.DECLINED) {
            throw new InvalidTripStateException("User is already in " + p.getStatus() + " state");
        }

        p.setStatus(TripParticipantStatus.LEFT);
        p.setLeftAt(LocalDateTime.now());
    }

    public void removeParticipant(String userId) {
        checkMutable();
        if (userId.equals(this.organizerId)) {
            throw new InvalidTripStateException("The organizer cannot be removed from the trip");
        }
        TripParticipantEntity p = findParticipant(userId)
                .orElseThrow(() -> new InvalidTripStateException("User is not a participant in this trip"));
        
        p.setStatus(TripParticipantStatus.REMOVED);
        p.setLeftAt(LocalDateTime.now());
    }

    private void checkMutable() {
        if (this.status == TripStatus.COMPLETED) {
            throw new InvalidTripStateException("Completed trips are read-only");
        }
        if (this.status == TripStatus.CANCELLED) {
            throw new InvalidTripStateException("Cancelled trips are read-only");
        }
    }

    private Optional<TripParticipantEntity> findParticipant(String userId) {
        return this.participants.stream()
                .filter(p -> p.getUserId().equals(userId))
                .findFirst();
    }
}
