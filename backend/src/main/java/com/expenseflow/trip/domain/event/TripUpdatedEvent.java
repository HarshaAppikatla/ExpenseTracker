package com.expenseflow.trip.domain.event;

import com.expenseflow.core.event.ApplicationEvent;

public class TripUpdatedEvent extends ApplicationEvent {
    private final String tripId;
    private final String title;
    private final String actorId;

    public TripUpdatedEvent(Object source, String tripId, String title, String actorId) {
        super(source);
        this.tripId = tripId;
        this.title = title;
        this.actorId = actorId;
    }

    public String getTripId() { return tripId; }
    public String getTitle() { return title; }
    public String getActorId() { return actorId; }
}
