package com.expenseflow.event.authentication;

import com.expenseflow.entity.UserEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {
    private final UserEntity user;
    private final String verificationUrl;

    public UserRegisteredEvent(Object source, UserEntity user, String verificationUrl) {
        super(source);
        this.user = user;
        this.verificationUrl = verificationUrl;
    }
}
