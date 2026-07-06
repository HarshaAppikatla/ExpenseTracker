package com.expenseflow.trip.service.command;

import com.expenseflow.trip.dto.CreateTripRequest;
import com.expenseflow.trip.dto.TripDto;
import com.expenseflow.trip.dto.UpdateTripRequest;

public interface TripCommandService {

    TripDto createTrip(CreateTripRequest request, String currentUserId);

    TripDto updateTrip(String tripId, UpdateTripRequest request, String currentUserId);

    TripDto updateStatus(String tripId, String status, String currentUserId);

    void deleteTrip(String tripId, String currentUserId);
}
