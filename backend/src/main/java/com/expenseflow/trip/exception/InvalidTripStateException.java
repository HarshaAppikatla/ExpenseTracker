package com.expenseflow.trip.exception;

public class InvalidTripStateException extends TripException {
    public InvalidTripStateException(String message) {
        super(message, "TRIP_102");
    }
}
