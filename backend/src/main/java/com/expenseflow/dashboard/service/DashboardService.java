package com.expenseflow.dashboard.service;

import com.expenseflow.dashboard.dto.CategoryBreakdownDto;
import com.expenseflow.dashboard.dto.DashboardResponse;
import com.expenseflow.dashboard.dto.TransactionDto;
import com.expenseflow.expense.domain.entity.ExpenseEntity;
import com.expenseflow.expense.domain.repository.ExpenseRepository;
import com.expenseflow.income.entity.IncomeEntity;
import com.expenseflow.income.repository.IncomeRepository;
import com.expenseflow.profile.entity.UserProfileEntity;
import com.expenseflow.profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserProfileRepository userProfileRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

    public DashboardResponse getDashboardSummary(String userId) {
        // Resolve profile metrics
        UserProfileEntity profile = userProfileRepository.findByUserId(userId).orElse(null);
        BigDecimal openingBal = profile != null && profile.getOpeningBalance() != null ? profile.getOpeningBalance() : BigDecimal.ZERO;

        BigDecimal totalInc = incomeRepository.sumIncomeByUserId(userId);
        BigDecimal totalExp = expenseRepository.sumExpensesByUserId(userId);

        BigDecimal netBal = openingBal.add(totalInc).subtract(totalExp);

        // Resolve breakdown
        List<Object[]> rawBreakdown = expenseRepository.getCategoryBreakdownRaw(userId);
        List<CategoryBreakdownDto> breakdown = new ArrayList<>();
        for (Object[] row : rawBreakdown) {
            com.expenseflow.expense.domain.valueobject.ExpenseCategory cat = (com.expenseflow.expense.domain.valueobject.ExpenseCategory) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            String name = cat.name();
            String color;
            switch (cat) {
                case FOOD: color = "#F59E0B"; break;
                case LODGING: color = "#6366F1"; break;
                case TRANSPORT: color = "#0EA5E9"; break;
                case ENTERTAINMENT: color = "#A855F7"; break;
                case SHOPPING: color = "#EC4899"; break;
                default: color = "#64748B"; break;
            }
            breakdown.add(new CategoryBreakdownDto(name, color, amount));
        }

        // Resolve recent transactions (limit to 10 combined)
        List<ExpenseEntity> expenses = expenseRepository.findAllUserExpenses(userId).stream()
                .sorted((e1, e2) -> e2.getExpenseDate().compareTo(e1.getExpenseDate()))
                .limit(10)
                .collect(Collectors.toList());

        List<IncomeEntity> incomes = incomeRepository.findByUserIdAndIsDeletedFalse(
                userId, PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "incomeDate"))
        ).getContent();

        List<TransactionDto> combined = new ArrayList<>();

        for (ExpenseEntity e : expenses) {
            String color;
            switch (e.getCategory()) {
                case FOOD: color = "#F59E0B"; break;
                case LODGING: color = "#6366F1"; break;
                case TRANSPORT: color = "#0EA5E9"; break;
                case ENTERTAINMENT: color = "#A855F7"; break;
                case SHOPPING: color = "#EC4899"; break;
                default: color = "#64748B"; break;
            }
            combined.add(new TransactionDto(
                    e.getId(),
                    "EXPENSE",
                    e.getMoney().getAmount(),
                    e.getMoney().getCurrency().name(),
                    e.getExpenseDate().atStartOfDay(),
                    e.getCategory().name(),
                    color,
                    null,
                    e.getDescription(),
                    Collections.emptyList()
            ));
        }

        for (IncomeEntity i : incomes) {
            combined.add(new TransactionDto(
                    i.getId(),
                    "INCOME",
                    i.getAmount(),
                    i.getCurrencyCode(),
                    i.getIncomeDate(),
                    "Income",
                    "#2ECC71",
                    i.getSource(),
                    i.getDescription(),
                    Collections.emptyList()
            ));
        }

        // Sort by date desc
        combined.sort((t1, t2) -> t2.date().compareTo(t1.date()));

        // Limit to top 10
        List<TransactionDto> recent = combined.stream().limit(10).collect(Collectors.toList());

        log.info("Dashboard summary resolved dynamically for user {}", userId);
        return new DashboardResponse(openingBal, totalInc, totalExp, netBal, breakdown, recent);
    }
}
