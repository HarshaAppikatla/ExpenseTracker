package com.expenseflow.util.impl;

import com.expenseflow.util.TimeProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;

@Component
public class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    @Override
    public Instant nowInstant() {
        return Instant.now();
    }
}
