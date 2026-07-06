package com.expenseflow.event.security;

import com.expenseflow.dto.auth.LoginContext;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReplayDetectedEvent extends ApplicationEvent {
    private final String userId;
    private final String tokenFamily;
    private final LoginContext loginContext;

    public ReplayDetectedEvent(Object source, String userId, String tokenFamily, LoginContext loginContext) {
        super(source);
        this.userId = userId;
        this.tokenFamily = tokenFamily;
        this.loginContext = loginContext;
    }
}
