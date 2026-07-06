package com.expenseflow.util;

import java.time.Instant;
import java.time.LocalDateTime;

public interface TimeProvider {
    LocalDateTime now();
    Instant nowInstant();
}
