package com.expenseflow.group.event;

import com.expenseflow.core.event.ApplicationEvent;

public class GroupDeletedEvent extends ApplicationEvent {
    private final String groupId;
    private final String actorId;

    public GroupDeletedEvent(Object source, String groupId, String actorId) {
        super(source);
        this.groupId = groupId;
        this.actorId = actorId;
    }

    public String getGroupId() { return groupId; }
    public String getActorId() { return actorId; }
}
