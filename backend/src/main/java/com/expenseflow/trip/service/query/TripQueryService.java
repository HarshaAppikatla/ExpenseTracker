package com.expenseflow.trip.service.query;

import com.expenseflow.trip.dto.TripDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TripQueryService {

    TripDto getTripDetails(String tripId, String currentUserId);

    Page<TripDto> getGroupTrips(String groupId, String currentUserId, Pageable pageable);
}
