package com.expenseflow.trip.service.query;

import com.expenseflow.trip.dto.TripActivityDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TimelineQueryService {

    Page<TripActivityDto> getTripTimeline(String tripId, String currentUserId, Pageable pageable);
}
