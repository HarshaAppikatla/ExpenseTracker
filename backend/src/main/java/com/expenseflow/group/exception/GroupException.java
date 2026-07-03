package com.expenseflow.group.exception;

import lombok.Getter;

@Getter
public abstract class GroupException extends RuntimeException {
    private final String code;

    protected GroupException(String message, String code) {
        super(message);
        this.code = code;
    }
}
