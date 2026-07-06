package com.expenseflow.insight.service;

import com.expenseflow.insight.dto.InsightDashboardDto;
import com.expenseflow.insight.provider.InsightProvider;
import com.expenseflow.core.calendar.FinancialCalendar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InsightService {

    private final List<InsightProvider> insightProviders;
    private final FinancialCalendar financialCalendar;

    public InsightDashboardDto getDashboardInsights(String userId) {
        LocalDateTime start = financialCalendar.currentMonthStart();
        LocalDateTime end = financialCalendar.currentMonthEnd();

        InsightDashboardDto dto = new InsightDashboardDto();

        for (InsightProvider provider : insightProviders) {
            try {
                provider.populateInsights(userId, start, end, dto);
            } catch (Exception e) {
                log.error("Error populating insights with provider {}: {}", provider.getClass().getSimpleName(), e.getMessage());
            }
        }

        return dto;
    }
}
