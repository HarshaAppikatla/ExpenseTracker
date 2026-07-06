package com.expenseflow.event.security;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.dto.auth.LoginContext;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AccountLockedEvent extends ApplicationEvent {
    private final UserEntity user;
    private final LoginContext loginContext;
    private final long lockDurationMinutes;

    public AccountLockedEvent(Object source, UserEntity user, LoginContext loginContext, long lockDurationMinutes) {
        super(source);
        this.user = user;
        this.loginContext = loginContext;
        this.lockDurationMinutes = lockDurationMinutes;
    }
}
