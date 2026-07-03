package com.expenseflow.group.event;

import com.expenseflow.core.event.ApplicationEvent;

public class OwnershipTransferredEvent extends ApplicationEvent {
    private final String groupId;
    private final String previousOwnerId;
    private final String newOwnerId;

    public OwnershipTransferredEvent(Object source, String groupId, String previousOwnerId, String newOwnerId) {
        super(source);
        this.groupId = groupId;
        this.previousOwnerId = previousOwnerId;
        this.newOwnerId = newOwnerId;
    }

    public String getGroupId() { return groupId; }
    public String getPreviousOwnerId() { return previousOwnerId; }
    public String getNewOwnerId() { return newOwnerId; }
}
