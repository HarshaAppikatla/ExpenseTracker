package com.expenseflow.group.entity;

public record GroupCode(String value) {
    public GroupCode {
        if (value == null || !value.matches("^[A-Z0-9]{8}$")) {
            throw new IllegalArgumentException("Group code must be exactly 8 uppercase alphanumeric characters");
        }
    }
}
