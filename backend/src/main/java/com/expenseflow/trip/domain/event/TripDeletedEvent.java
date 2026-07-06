package com.expenseflow.trip.domain.event;

import com.expenseflow.core.event.ApplicationEvent;

public class TripDeletedEvent extends ApplicationEvent {
    private final String tripId;
    private final String actorId;

    public TripDeletedEvent(Object source, String tripId, String actorId) {
        super(source);
        this.tripId = tripId;
        this.actorId = actorId;
    }

    public String getTripId() { return tripId; }
    public String getActorId() { return actorId; }
}
