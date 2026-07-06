package com.expenseflow.trip.exception;

public class ParticipantConflictException extends TripException {
    public ParticipantConflictException(String message) {
        super(message, "TRIP_103");
    }
}
