package com.expenseflow.core.event;

import java.time.Instant;

public abstract class ApplicationEvent extends org.springframework.context.ApplicationEvent {
    private final Instant occurredOn;

    protected ApplicationEvent(Object source) {
        super(source);
        this.occurredOn = Instant.now();
    }

    public Instant getOccurredOn() {
        return occurredOn;
    }
}
