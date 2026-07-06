package com.expenseflow.trip.domain.event;

import com.expenseflow.core.event.ApplicationEvent;

public class ParticipantRemovedEvent extends ApplicationEvent {
    private final String tripId;
    private final String userId;
    private final String actorId;

    public ParticipantRemovedEvent(Object source, String tripId, String userId, String actorId) {
        super(source);
        this.tripId = tripId;
        this.userId = userId;
        this.actorId = actorId;
    }

    public String getTripId() { return tripId; }
    public String getUserId() { return userId; }
    public String getActorId() { return actorId; }
}
