package com.expenseflow.group.event;

import com.expenseflow.core.event.ApplicationEvent;

public class MemberJoinedEvent extends ApplicationEvent {
    private final String groupId;
    private final String userId;

    public MemberJoinedEvent(Object source, String groupId, String userId) {
        super(source);
        this.groupId = groupId;
        this.userId = userId;
    }

    public String getGroupId() { return groupId; }
    public String getUserId() { return userId; }
}
