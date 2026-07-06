package com.expenseflow.exception;

import lombok.Getter;

@Getter
public class SecurityHardeningException extends RuntimeException {
    
    private final String code;
    private final Object data;

    public SecurityHardeningException(String message, String code) {
        super(message);
        this.code = code;
        this.data = null;
    }

    public SecurityHardeningException(String message, String code, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }
}
