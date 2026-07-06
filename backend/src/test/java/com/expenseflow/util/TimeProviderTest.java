package com.expenseflow.util;

import com.expenseflow.util.impl.SystemTimeProvider;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class TimeProviderTest {

    private final TimeProvider timeProvider = new SystemTimeProvider();

    @Test
    void testNow_ReturnsCurrentLocalDateTime() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        LocalDateTime now = timeProvider.now();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertThat(now).isAfterOrEqualTo(before);
        assertThat(now).isBeforeOrEqualTo(after);
    }

    @Test
    void testNowInstant_ReturnsCurrentInstant() {
        Instant now = timeProvider.nowInstant();
        assertThat(now).isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS));
    }
}
