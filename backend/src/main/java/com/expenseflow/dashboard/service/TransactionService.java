package com.expenseflow.dashboard.service;

import com.expenseflow.dashboard.dto.TransactionDto;
import com.expenseflow.expense.domain.entity.ExpenseEntity;
import com.expenseflow.expense.domain.repository.ExpenseRepository;
import com.expenseflow.income.entity.IncomeEntity;
import com.expenseflow.income.repository.IncomeRepository;
import com.expenseflow.income.specification.IncomeSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

    public Page<TransactionDto> searchTransactions(
            String userId,
            String type,
            String category,
            String sourceOrMerchant,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            String description,
            String tag,
            Pageable pageable) {

        List<TransactionDto> combined = new ArrayList<>();

        boolean includeExpenses = (type == null || type.trim().isEmpty() || "EXPENSE".equalsIgnoreCase(type));
        boolean includeIncome = (type == null || type.trim().isEmpty() || "INCOME".equalsIgnoreCase(type));

        // 1. Fetch filtered Expenses
        if (includeExpenses) {
            List<ExpenseEntity> expenses = expenseRepository.findAllUserExpenses(userId);
            
            // In-memory filter to match search criteria
            List<ExpenseEntity> filteredExpenses = expenses.stream()
                .filter(e -> {
                    if (minAmount != null && e.getMoney().getAmount().compareTo(minAmount) < 0) return false;
                    if (maxAmount != null && e.getMoney().getAmount().compareTo(maxAmount) > 0) return false;
                    if (fromDate != null && e.getExpenseDate().atStartOfDay().isBefore(fromDate)) return false;
                    if (toDate != null && e.getExpenseDate().atStartOfDay().isAfter(toDate)) return false;
                    if (category != null && !category.trim().isEmpty()) {
                        try {
                            com.expenseflow.expense.domain.valueobject.ExpenseCategory filterCat = com.expenseflow.expense.domain.valueobject.ExpenseCategory.valueOf(category.toUpperCase());
                            if (e.getCategory() != filterCat) return false;
                        } catch (IllegalArgumentException ex) {
                            return false;
                        }
                    }
                    if (description != null && !description.trim().isEmpty() && 
                        (e.getDescription() == null || !e.getDescription().toLowerCase().contains(description.toLowerCase()))) {
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

            for (ExpenseEntity e : filteredExpenses) {
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
        }

        // 2. Fetch filtered Incomes
        // (Note: Income doesn't have categories, merchants or tags, so if those filters are specified, income matches 0)
        boolean hasExpenseOnlyFilters = (category != null && !category.trim().isEmpty()) || (tag != null && !tag.trim().isEmpty());
        if (includeIncome && !hasExpenseOnlyFilters) {
            Specification<IncomeEntity> spec = Specification.where(IncomeSpecification.hasUserId(userId))
                    .and(IncomeSpecification.isNotDeleted());

            if (sourceOrMerchant != null && !sourceOrMerchant.trim().isEmpty()) {
                spec = spec.and(IncomeSpecification.searchSource(sourceOrMerchant));
            }
            if (minAmount != null) {
                spec = spec.and(IncomeSpecification.amountGreaterThanOrEqual(minAmount));
            }
            if (maxAmount != null) {
                spec = spec.and(IncomeSpecification.amountLessThanOrEqual(maxAmount));
            }
            if (fromDate != null) {
                spec = spec.and(IncomeSpecification.dateAfterOrEqual(fromDate));
            }
            if (toDate != null) {
                spec = spec.and(IncomeSpecification.dateBeforeOrEqual(toDate));
            }
            if (description != null && !description.trim().isEmpty()) {
                spec = spec.and(IncomeSpecification.hasDescription(description));
            }

            List<IncomeEntity> incomes = incomeRepository.findAll(spec);
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
        }

        // 3. Sort the combined list by date desc
        combined.sort((t1, t2) -> t2.date().compareTo(t1.date()));

        // 4. Apply pagination sublisting safely
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), combined.size());

        List<TransactionDto> paginatedList = new ArrayList<>();
        if (start < combined.size()) {
            paginatedList = combined.subList(start, end);
        }

        log.info("Unified transaction search resolved {} matches for user {}", combined.size(), userId);
        return new PageImpl<>(paginatedList, pageable, combined.size());
    }
}
