package com.expenseflow.insight.provider;

import com.expenseflow.insight.dto.InsightDashboardDto;
import java.time.LocalDateTime;

public interface InsightProvider {
    void populateInsights(String userId, LocalDateTime start, LocalDateTime end, InsightDashboardDto dto);
}
