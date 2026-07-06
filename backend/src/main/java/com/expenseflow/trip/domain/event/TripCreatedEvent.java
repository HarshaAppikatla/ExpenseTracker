package com.expenseflow.trip.domain.event;

import com.expenseflow.core.event.ApplicationEvent;

public class TripCreatedEvent extends ApplicationEvent {
    private final String tripId;
    private final String title;
    private final String groupId;
    private final String organizerId;

    public TripCreatedEvent(Object source, String tripId, String title, String groupId, String organizerId) {
        super(source);
        this.tripId = tripId;
        this.title = title;
        this.groupId = groupId;
        this.organizerId = organizerId;
    }

    public String getTripId() { return tripId; }
    public String getTitle() { return title; }
    public String getGroupId() { return groupId; }
    public String getOrganizerId() { return organizerId; }
}
