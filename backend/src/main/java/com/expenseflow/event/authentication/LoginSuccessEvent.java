package com.expenseflow.event.authentication;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.dto.auth.LoginContext;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class LoginSuccessEvent extends ApplicationEvent {
    private final UserEntity user;
    private final LoginContext loginContext;

    public LoginSuccessEvent(Object source, UserEntity user, LoginContext loginContext) {
        super(source);
        this.user = user;
        this.loginContext = loginContext;
    }
}
