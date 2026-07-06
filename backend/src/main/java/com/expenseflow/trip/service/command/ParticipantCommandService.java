package com.expenseflow.trip.service.command;

import com.expenseflow.trip.dto.TripDto;

public interface ParticipantCommandService {

    TripDto inviteParticipant(String tripId, String userId, String currentUserId);

    TripDto acceptInvite(String tripId, String currentUserId);

    TripDto declineInvite(String tripId, String currentUserId);

    TripDto leaveTrip(String tripId, String currentUserId);

    TripDto removeParticipant(String tripId, String userId, String currentUserId);
}
