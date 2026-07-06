package com.expenseflow.event.security;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RateLimitExceededEvent extends ApplicationEvent {
    private final String ipAddress;
    private final String endpointType;

    public RateLimitExceededEvent(Object source, String ipAddress, String endpointType) {
        super(source);
        this.ipAddress = ipAddress;
        this.endpointType = endpointType;
    }
}
