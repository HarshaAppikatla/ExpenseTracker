package com.expenseflow.trip.domain.entity;

import com.expenseflow.trip.domain.valueobject.*;
import com.expenseflow.trip.exception.InvalidTripStateException;
import com.expenseflow.trip.exception.ParticipantConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.ArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TripEntityTest {

    private TripEntity trip;
    private final String organizerId = "user-organizer";
    private final String memberId = "user-member";

    @BeforeEach
    void setUp() {
        trip = TripEntity.builder()
                .id("trip-1")
                .groupId("group-1")
                .title("Paris Trip")
                .destination(new Destination("Paris", "France", "Paris, France"))
                .schedule(new TripSchedule(LocalDate.now(), LocalDate.now().plusDays(5)))
                .organizerId(organizerId)
                .status(TripStatus.PLANNING)
                .settings(new TripSettings())
                .participants(new ArrayList<>())
                .build();

        // Add organizer as participant
        TripParticipantEntity organizer = TripParticipantEntity.builder()
                .id("part-org")
                .trip(trip)
                .userId(organizerId)
                .status(TripParticipantStatus.ACCEPTED)
                .build();
        trip.getParticipants().add(organizer);
    }

    @Test
    void scheduleValidation_ShouldThrowException_WhenStartDateAfterEndDate() {
        TripSchedule invalidSchedule = new TripSchedule(LocalDate.now().plusDays(5), LocalDate.now());
        assertThatThrownBy(invalidSchedule::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start date must be before or equal to end date");
    }

    @Test
    void updateMetadata_ShouldSucceed_WhenPlanning() {
        Destination newDest = new Destination("Rome", "Italy", "Rome, Italy");
        TripSchedule newSched = new TripSchedule(LocalDate.now().plusDays(1), LocalDate.now().plusDays(6));

        trip.updateMetadata("Rome Trip", "Colosseum trip", newDest, newSched, null, "CUSTOM", "http://image");

        assertThat(trip.getTitle()).isEqualTo("Rome Trip");
        assertThat(trip.getDestination().getCity()).isEqualTo("Rome");
        assertThat(trip.getSchedule().getStartDate()).isEqualTo(newSched.getStartDate());
    }

    @Test
    void updateMetadata_ShouldThrowException_WhenCompleted() {
        trip.setStatus(TripStatus.COMPLETED);
        Destination newDest = new Destination("Rome", "Italy", "Rome, Italy");

        assertThatThrownBy(() -> trip.updateMetadata("Rome Trip", "Colosseum", newDest, null, null, null, null))
                .isInstanceOf(InvalidTripStateException.class)
                .hasMessageContaining("Completed trips are read-only");
    }

    @Test
    void transitionTo_ShouldFollowTransitionsFlow() {
        // PLANNING -> ACTIVE (Allowed)
        trip.transitionTo(TripStatus.ACTIVE);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.ACTIVE);

        // ACTIVE -> COMPLETED (Allowed)
        trip.transitionTo(TripStatus.COMPLETED);
        assertThat(trip.getStatus()).isEqualTo(TripStatus.COMPLETED);
    }

    @Test
    void transitionTo_ShouldThrowException_WhenInvalidTransition() {
        // PLANNING -> COMPLETED (Not Allowed)
        assertThatThrownBy(() -> trip.transitionTo(TripStatus.COMPLETED))
                .isInstanceOf(InvalidTripStateException.class);

        // ACTIVE -> CANCELLED (Not Allowed)
        trip.setStatus(TripStatus.ACTIVE);
        assertThatThrownBy(() -> trip.transitionTo(TripStatus.CANCELLED))
                .isInstanceOf(InvalidTripStateException.class);
    }

    @Test
    void inviteParticipant_ShouldSucceed_WhenUserNotAlreadyInvited() {
        trip.inviteParticipant(memberId);
        
        assertThat(trip.getParticipants()).hasSize(2);
        TripParticipantEntity p = trip.getParticipants().stream()
                .filter(part -> part.getUserId().equals(memberId))
                .findFirst().orElse(null);
        
        assertThat(p).isNotNull();
        assertThat(p.getStatus()).isEqualTo(TripParticipantStatus.INVITED);
    }

    @Test
    void inviteParticipant_ShouldThrowException_WhenUserAlreadyActive() {
        // Organizer is already accepted
        assertThatThrownBy(() -> trip.inviteParticipant(organizerId))
                .isInstanceOf(ParticipantConflictException.class);
    }

    @Test
    void acceptInvite_ShouldSucceed_WhenInvited() {
        trip.inviteParticipant(memberId);
        trip.acceptInvite(memberId);

        TripParticipantEntity p = trip.getParticipants().stream()
                .filter(part -> part.getUserId().equals(memberId))
                .findFirst().orElse(null);

        assertThat(p).isNotNull();
        assertThat(p.getStatus()).isEqualTo(TripParticipantStatus.ACCEPTED);
        assertThat(p.getJoinedAt()).isNotNull();
    }

    @Test
    void acceptInvite_ShouldThrowException_WhenNotInvited() {
        assertThatThrownBy(() -> trip.acceptInvite(memberId))
                .isInstanceOf(InvalidTripStateException.class);
    }

    @Test
    void leave_ShouldSucceed_WhenParticipant() {
        trip.inviteParticipant(memberId);
        trip.acceptInvite(memberId);

        trip.leave(memberId);

        TripParticipantEntity p = trip.getParticipants().stream()
                .filter(part -> part.getUserId().equals(memberId))
                .findFirst().orElse(null);

        assertThat(p).isNotNull();
        assertThat(p.getStatus()).isEqualTo(TripParticipantStatus.LEFT);
        assertThat(p.getLeftAt()).isNotNull();
    }

    @Test
    void leave_ShouldThrowException_WhenOrganizerTriesToLeave() {
        assertThatThrownBy(() -> trip.leave(organizerId))
                .isInstanceOf(InvalidTripStateException.class)
                .hasMessageContaining("The organizer cannot leave the trip");
    }

    @Test
    void removeParticipant_ShouldSucceed_WhenActive() {
        trip.inviteParticipant(memberId);
        trip.acceptInvite(memberId);

        trip.removeParticipant(memberId);

        TripParticipantEntity p = trip.getParticipants().stream()
                .filter(part -> part.getUserId().equals(memberId))
                .findFirst().orElse(null);

        assertThat(p).isNotNull();
        assertThat(p.getStatus()).isEqualTo(TripParticipantStatus.REMOVED);
    }

    @Test
    void removeParticipant_ShouldThrowException_WhenOrganizerRemoved() {
        assertThatThrownBy(() -> trip.removeParticipant(organizerId))
                .isInstanceOf(InvalidTripStateException.class)
                .hasMessageContaining("The organizer cannot be removed from the trip");
    }
}
