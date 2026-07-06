package com.expenseflow.insight.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InsightDashboardDto {
    private BigDecimal totalSpentCurrentMonth;
    private BigDecimal totalIncomeCurrentMonth;
    private BigDecimal netSavingsCurrentMonth;
    private BigDecimal budgetLimitTotal;
    private BigDecimal budgetSpentTotal;
    private double budgetUtilizationRate;
    private List<CategorySpend> topSpendingCategories;
    private List<MonthlyTrend> monthlyTrends;
    private List<SavingsGoalProgress> savingsGoalsProgress;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategorySpend {
        private String categoryId;
        private String categoryName;
        private BigDecimal amount;
        private String color;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MonthlyTrend {
        private String monthName;
        private BigDecimal income;
        private BigDecimal expense;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SavingsGoalProgress {
        private String goalId;
        private String title;
        private BigDecimal targetAmount;
        private BigDecimal currentAmount;
        private double progress;
    }
}
