package com.expenseflow.trip.exception;

public class PermissionDeniedException extends TripException {
    public PermissionDeniedException(String message) {
        super(message, "TRIP_104");
    }
}
