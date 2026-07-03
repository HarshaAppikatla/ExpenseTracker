package com.expenseflow.group.event;

import com.expenseflow.core.event.ApplicationEvent;

public class RoleChangedEvent extends ApplicationEvent {
    private final String groupId;
    private final String userId;
    private final String oldRole;
    private final String newRole;
    private final String actorId;

    public RoleChangedEvent(Object source, String groupId, String userId, String oldRole, String newRole, String actorId) {
        super(source);
        this.groupId = groupId;
        this.userId = userId;
        this.oldRole = oldRole;
        this.newRole = newRole;
        this.actorId = actorId;
    }

    public String getGroupId() { return groupId; }
    public String getUserId() { return userId; }
    public String getOldRole() { return oldRole; }
    public String getNewRole() { return newRole; }
    public String getActorId() { return actorId; }
}
