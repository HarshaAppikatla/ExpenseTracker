package com.expenseflow.insight.provider;

import com.expenseflow.budget.repository.BudgetStatisticsRepository;
import com.expenseflow.insight.dto.InsightDashboardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CategoryInsightProvider implements InsightProvider {

    private final BudgetStatisticsRepository budgetStatisticsRepository;

    @Override
    public void populateInsights(String userId, LocalDateTime start, LocalDateTime end, InsightDashboardDto dto) {
        List<Object[]> rows = budgetStatisticsRepository.getCategoryBreakdownForDateRange(userId, start, end);
        
        List<InsightDashboardDto.CategorySpend> list = rows.stream()
                .map(row -> new InsightDashboardDto.CategorySpend(
                        (String) row[0],
                        (String) row[1],
                        (BigDecimal) row[2],
                        (String) row[3]
                ))
                .collect(Collectors.toList());

        dto.setTopSpendingCategories(list);
    }
}
