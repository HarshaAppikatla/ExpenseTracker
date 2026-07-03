package com.expenseflow.group.event;

import com.expenseflow.core.event.ApplicationEvent;

public class GroupArchivedEvent extends ApplicationEvent {
    private final String groupId;
    private final String actorId;

    public GroupArchivedEvent(Object source, String groupId, String actorId) {
        super(source);
        this.groupId = groupId;
        this.actorId = actorId;
    }

    public String getGroupId() { return groupId; }
    public String getActorId() { return actorId; }
}
