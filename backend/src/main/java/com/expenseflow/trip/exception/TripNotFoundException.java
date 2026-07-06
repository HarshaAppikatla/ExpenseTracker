package com.expenseflow.trip.exception;

public class TripNotFoundException extends TripException {
    public TripNotFoundException(String message) {
        super(message, "TRIP_101");
    }
}
