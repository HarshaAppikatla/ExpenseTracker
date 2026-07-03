package com.expenseflow.group.event;

import com.expenseflow.core.event.ApplicationEvent;

public class MemberRemovedEvent extends ApplicationEvent {
    private final String groupId;
    private final String userId;
    private final String actorId;

    public MemberRemovedEvent(Object source, String groupId, String userId, String actorId) {
        super(source);
        this.groupId = groupId;
        this.userId = userId;
        this.actorId = actorId;
    }

    public String getGroupId() { return groupId; }
    public String getUserId() { return userId; }
    public String getActorId() { return actorId; }
}
