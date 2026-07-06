package com.expenseflow.trip.domain.validator;

import com.expenseflow.trip.domain.entity.TripEntity;
import com.expenseflow.trip.domain.entity.TripParticipantEntity;
import com.expenseflow.trip.domain.valueobject.TripParticipantStatus;
import com.expenseflow.trip.exception.InvalidTripStateException;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;

@Component
public class TripValidator {

    public void validateInvariants(TripEntity trip) {
        if (trip.getOrganizerId() == null) {
            throw new InvalidTripStateException("Trip must have an organizer");
        }

        if (trip.getSchedule() != null) {
            trip.getSchedule().validate();
        }

        if (trip.getDestination() == null) {
            throw new InvalidTripStateException("Trip must have a destination");
        }

        // Verify organizer is a participant in accepted state
        boolean organizerValid = trip.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(trip.getOrganizerId()) &&
                        p.getStatus() == TripParticipantStatus.ACCEPTED);

        if (!organizerValid) {
            throw new InvalidTripStateException("The organizer must be an active participant of the trip");
        }

        // Verify no duplicate participants
        Set<String> userIds = new HashSet<>();
        for (TripParticipantEntity p : trip.getParticipants()) {
            if (!userIds.add(p.getUserId())) {
                throw new InvalidTripStateException("Duplicate participant detected: " + p.getUserId());
            }
        }
    }
}
