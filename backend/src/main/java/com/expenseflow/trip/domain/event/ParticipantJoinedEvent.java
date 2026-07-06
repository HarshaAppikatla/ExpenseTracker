package com.expenseflow.trip.domain.event;

import com.expenseflow.core.event.ApplicationEvent;

public class ParticipantJoinedEvent extends ApplicationEvent {
    private final String tripId;
    private final String userId;

    public ParticipantJoinedEvent(Object source, String tripId, String userId) {
        super(source);
        this.tripId = tripId;
        this.userId = userId;
    }

    public String getTripId() { return tripId; }
    public String getUserId() { return userId; }
}
