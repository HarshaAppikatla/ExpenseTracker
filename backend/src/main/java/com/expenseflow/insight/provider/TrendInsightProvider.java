package com.expenseflow.insight.provider;

import com.expenseflow.budget.repository.BudgetStatisticsRepository;
import com.expenseflow.income.repository.IncomeRepository;
import com.expenseflow.insight.dto.InsightDashboardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TrendInsightProvider implements InsightProvider {

    private final BudgetStatisticsRepository expenseStatsRepository;
    private final IncomeRepository incomeRepository;

    @Override
    public void populateInsights(String userId, LocalDateTime start, LocalDateTime end, InsightDashboardDto dto) {
        BigDecimal totalSpent = expenseStatsRepository.sumExpensesByUserIdAndDateRange(userId, start, end);
        BigDecimal totalIncome = incomeRepository.sumIncomeByUserIdAndDateRange(userId, start, end);
        BigDecimal netSavings = totalIncome.subtract(totalSpent);

        dto.setTotalSpentCurrentMonth(totalSpent);
        dto.setTotalIncomeCurrentMonth(totalIncome);
        dto.setNetSavingsCurrentMonth(netSavings);

        List<InsightDashboardDto.MonthlyTrend> trends = new ArrayList<>();
        
        for (int i = 2; i >= 0; i--) {
            LocalDateTime mStart = start.minusMonths(i);
            LocalDateTime mEnd = mStart.plusMonths(1).minusNanos(1);
            String monthName = mStart.getMonth().name().substring(0, 3);

            BigDecimal expSum = expenseStatsRepository.sumExpensesByUserIdAndDateRange(userId, mStart, mEnd);
            BigDecimal incSum = incomeRepository.sumIncomeByUserIdAndDateRange(userId, mStart, mEnd);

            trends.add(new InsightDashboardDto.MonthlyTrend(
                    monthName,
                    incSum,
                    expSum
            ));
        }

        dto.setMonthlyTrends(trends);
    }
}
