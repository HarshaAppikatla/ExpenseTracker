package com.expenseflow.group.event;

import com.expenseflow.core.event.ApplicationEvent;

public class GroupCreatedEvent extends ApplicationEvent {
    private final String groupId;
    private final String name;
    private final String ownerId;

    public GroupCreatedEvent(Object source, String groupId, String name, String ownerId) {
        super(source);
        this.groupId = groupId;
        this.name = name;
        this.ownerId = ownerId;
    }

    public String getGroupId() { return groupId; }
    public String getName() { return name; }
    public String getOwnerId() { return ownerId; }
}
