package com.expenseflow.event.authentication;

import com.expenseflow.dto.auth.LoginContext;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LoginFailureEvent extends ApplicationEvent {
    private final String email;
    private final LoginContext loginContext;
    private final String reason;

    public LoginFailureEvent(Object source, String email, LoginContext loginContext, String reason) {
        super(source);
        this.email = email;
        this.loginContext = loginContext;
        this.reason = reason;
    }
}
