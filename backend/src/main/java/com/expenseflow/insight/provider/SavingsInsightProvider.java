package com.expenseflow.insight.provider;

import com.expenseflow.insight.dto.InsightDashboardDto;
import com.expenseflow.savings.entity.SavingsGoalEntity;
import com.expenseflow.savings.repository.SavingsDepositRepository;
import com.expenseflow.savings.repository.SavingsGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SavingsInsightProvider implements InsightProvider {

    private final SavingsGoalRepository savingsGoalRepository;
    private final SavingsDepositRepository savingsDepositRepository;

    @Override
    public void populateInsights(String userId, LocalDateTime start, LocalDateTime end, InsightDashboardDto dto) {
        List<SavingsGoalEntity> goals = savingsGoalRepository.findByUserIdAndIsDeletedFalse(userId);

        List<InsightDashboardDto.SavingsGoalProgress> list = goals.stream()
                .map(goal -> {
                    BigDecimal sum = savingsDepositRepository.calculateSumByGoalId(goal.getId());
                    double progress = 0.0;
                    if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
                        progress = sum.divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
                    }
                    return new InsightDashboardDto.SavingsGoalProgress(
                            goal.getId(),
                            goal.getTitle(),
                            goal.getTargetAmount(),
                            sum,
                            progress
                    );
                })
                .collect(Collectors.toList());

        dto.setSavingsGoalsProgress(list);
    }
}
