package com.expenseflow.trip.exception;

import lombok.Getter;

@Getter
public abstract class TripException extends RuntimeException {
    private final String code;

    protected TripException(String message, String code) {
        super(message);
        this.code = code;
    }
}
