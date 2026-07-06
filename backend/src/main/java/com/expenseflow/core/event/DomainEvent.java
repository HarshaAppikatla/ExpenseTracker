package com.expenseflow.core.event;

import java.time.Instant;

public interface DomainEvent {
    Instant getOccurredOn();
}
