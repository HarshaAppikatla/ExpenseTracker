package com.expenseflow.insight.provider;

import com.expenseflow.budget.entity.BudgetEntity;
import com.expenseflow.budget.repository.BudgetRepository;
import com.expenseflow.budget.repository.BudgetStatisticsRepository;
import com.expenseflow.insight.dto.InsightDashboardDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BudgetInsightProvider implements InsightProvider {

    private final BudgetRepository budgetRepository;
    private final BudgetStatisticsRepository budgetStatisticsRepository;

    @Override
    public void populateInsights(String userId, LocalDateTime start, LocalDateTime end, InsightDashboardDto dto) {
        int year = start.getYear();
        int month = start.getMonthValue();

        List<BudgetEntity> budgets = budgetRepository.findByUserIdAndBudgetYearAndBudgetMonthAndIsDeletedFalse(userId, year, month);

        BigDecimal limitTotal = BigDecimal.ZERO;
        BigDecimal spentTotal = BigDecimal.ZERO;

        for (BudgetEntity budget : budgets) {
            if (budget.isActive()) {
                limitTotal = limitTotal.add(budget.getMonthlyLimit());
                BigDecimal spent = budgetStatisticsRepository.calculateSpendsForCategory(
                        userId, budget.getCategory().getId(), start, end
                );
                spentTotal = spentTotal.add(spent);
            }
        }

        dto.setBudgetLimitTotal(limitTotal);
        dto.setBudgetSpentTotal(spentTotal);

        double rate = 0.0;
        if (limitTotal.compareTo(BigDecimal.ZERO) > 0) {
            rate = spentTotal.divide(limitTotal, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        }
        dto.setBudgetUtilizationRate(rate);
    }
}
