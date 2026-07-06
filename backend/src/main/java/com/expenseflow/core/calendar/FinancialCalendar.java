package com.expenseflow.core.calendar;

import com.expenseflow.util.TimeProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Objects;

@Service
public class FinancialCalendar {
    private final TimeProvider timeProvider;

    public FinancialCalendar(TimeProvider timeProvider) {
        this.timeProvider = Objects.requireNonNull(timeProvider, "TimeProvider must not be null");
    }

    public LocalDate currentLocalDate() {
        return timeProvider.now().toLocalDate();
    }

    public YearMonth currentYearMonth() {
        return YearMonth.from(timeProvider.now());
    }

    public int currentMonthValue() {
        return timeProvider.now().getMonthValue();
    }

    public int currentYear() {
        return timeProvider.now().getYear();
    }

    public LocalDateTime monthStart(int year, int month) {
        return LocalDate.of(year, month, 1).atStartOfDay();
    }

    public LocalDateTime monthEnd(int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        return ym.atEndOfMonth().atTime(23, 59, 59, 999999999);
    }

    public LocalDateTime currentMonthStart() {
        LocalDateTime now = timeProvider.now();
        return monthStart(now.getYear(), now.getMonthValue());
    }

    public LocalDateTime currentMonthEnd() {
        LocalDateTime now = timeProvider.now();
        return monthEnd(now.getYear(), now.getMonthValue());
    }
}
