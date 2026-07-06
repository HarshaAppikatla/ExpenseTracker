package com.expenseflow.budget.service;

import com.expenseflow.budget.dto.BudgetDto;
import com.expenseflow.budget.dto.BudgetProgressDto;
import com.expenseflow.budget.dto.BudgetRequest;
import com.expenseflow.budget.entity.BudgetEntity;
import com.expenseflow.budget.mapper.BudgetMapper;
import com.expenseflow.budget.repository.BudgetRepository;
import com.expenseflow.budget.repository.BudgetStatisticsRepository;
import com.expenseflow.category.entity.CategoryEntity;
import com.expenseflow.category.repository.CategoryRepository;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.core.calendar.FinancialCalendar;
import com.expenseflow.core.event.BudgetCreatedEvent;
import com.expenseflow.core.event.BudgetDeletedEvent;
import com.expenseflow.core.event.BudgetLimitExceededEvent;
import com.expenseflow.core.event.BudgetUpdatedEvent;
import com.expenseflow.core.event.BudgetWarningEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetStatisticsRepository budgetStatisticsRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetMapper budgetMapper;
    private final FinancialCalendar financialCalendar;
    private final ApplicationEventPublisher eventPublisher;

    public BudgetDto createBudget(String userId, BudgetRequest request) {
        Optional<BudgetEntity> existing = budgetRepository.findByUserIdAndCategoryIdAndBudgetYearAndBudgetMonthAndIsDeletedFalse(
                userId, request.categoryId(), request.year(), request.month()
        );
        if (existing.isPresent()) {
            throw new SecurityHardeningException("Budget already configured for this category and month.", "BDG_003");
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new SecurityHardeningException("User not found.", "USR_001"));
        CategoryEntity category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new SecurityHardeningException("Category not found.", "CAT_001"));

        BudgetEntity entity = BudgetEntity.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .category(category)
                .budgetYear(request.year())
                .budgetMonth(request.month())
                .monthlyLimit(request.monthlyLimit())
                .currencyCode(request.currencyCode() != null ? request.currencyCode() : "USD")
                .alertPercentage(request.alertPercentage() != null ? request.alertPercentage() : 80)
                .active(true)
                .build();

        BudgetEntity saved = budgetRepository.save(entity);
        log.info("Budget created successfully: {} for user {}", saved.getId(), userId);

        eventPublisher.publishEvent(new BudgetCreatedEvent(this, saved.getId(), userId, category.getId(), saved.getMonthlyLimit()));
        return budgetMapper.toDto(saved);
    }

    public BudgetDto updateBudget(String userId, String budgetId, BudgetRequest request) {
        BudgetEntity entity = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new SecurityHardeningException("Budget not found.", "BDG_001"));

        if (!entity.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this budget.", "BDG_002");
        }

        entity.setMonthlyLimit(request.monthlyLimit());
        if (request.alertPercentage() != null) {
            entity.setAlertPercentage(request.alertPercentage());
        }
        if (request.currencyCode() != null) {
            entity.setCurrencyCode(request.currencyCode());
        }

        BudgetEntity saved = budgetRepository.save(entity);
        log.info("Budget updated successfully: {}", budgetId);

        eventPublisher.publishEvent(new BudgetUpdatedEvent(this, saved.getId(), userId, saved.getCategory().getId(), saved.getMonthlyLimit()));
        return budgetMapper.toDto(saved);
    }

    public void deleteBudget(String userId, String budgetId) {
        BudgetEntity entity = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new SecurityHardeningException("Budget not found.", "BDG_001"));

        if (!entity.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this budget.", "BDG_002");
        }

        entity.setDeleted(true);
        entity.setDeletedAt(LocalDateTime.now());
        budgetRepository.save(entity);
        log.info("Budget deleted successfully: {}", budgetId);

        eventPublisher.publishEvent(new BudgetDeletedEvent(this, budgetId, userId));
    }

    @Transactional(readOnly = true)
    public List<BudgetProgressDto> getBudgets(String userId, int year, int month) {
        List<BudgetEntity> budgets = budgetRepository.findByUserIdAndBudgetYearAndBudgetMonthAndIsDeletedFalse(userId, year, month);
        return budgets.stream()
                .map(this::calculateProgress)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BudgetProgressDto getBudgetProgress(String userId, String budgetId) {
        BudgetEntity entity = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new SecurityHardeningException("Budget not found.", "BDG_001"));

        if (!entity.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("Access denied. You do not own this budget.", "BDG_002");
        }

        return calculateProgress(entity);
    }

    public void checkBudgetUtilization(String userId, String categoryId, int year, int month) {
        Optional<BudgetEntity> optionalBudget = budgetRepository.findByUserIdAndCategoryIdAndBudgetYearAndBudgetMonthAndIsDeletedFalse(
                userId, categoryId, year, month
        );
        if (optionalBudget.isEmpty() || !optionalBudget.get().isActive()) {
            return;
        }

        BudgetEntity budget = optionalBudget.get();
        LocalDateTime start = financialCalendar.monthStart(year, month);
        LocalDateTime end = financialCalendar.monthEnd(year, month);

        BigDecimal spent = budgetStatisticsRepository.calculateSpendsForCategory(userId, categoryId, start, end);
        BigDecimal limit = budget.getMonthlyLimit();

        if (limit.compareTo(BigDecimal.ZERO) <= 0) return;

        double utilization = spent.divide(limit, 4, RoundingMode.HALF_UP).doubleValue() * 100.0;

        if (utilization >= 100.0) {
            eventPublisher.publishEvent(new BudgetLimitExceededEvent(
                    this, userId, categoryId, budget.getCategory().getName(), year, month, limit, spent
            ));
        } else if (utilization >= budget.getAlertPercentage()) {
            eventPublisher.publishEvent(new BudgetWarningEvent(
                    this, userId, categoryId, budget.getCategory().getName(), year, month, limit, spent, budget.getAlertPercentage()
            ));
        }
    }

    private BudgetProgressDto calculateProgress(BudgetEntity entity) {
        LocalDateTime start = financialCalendar.monthStart(entity.getBudgetYear(), entity.getBudgetMonth());
        LocalDateTime end = financialCalendar.monthEnd(entity.getBudgetYear(), entity.getBudgetMonth());

        BigDecimal spent = budgetStatisticsRepository.calculateSpendsForCategory(
                entity.getUser().getId(), entity.getCategory().getId(), start, end
        );

        BigDecimal remaining = entity.getMonthlyLimit().subtract(spent);
        double utilization = 0.0;
        if (entity.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0) {
            utilization = spent.divide(entity.getMonthlyLimit(), 4, RoundingMode.HALF_UP).doubleValue() * 100.0;
        }

        return new BudgetProgressDto(
                budgetMapper.toDto(entity),
                spent,
                remaining,
                utilization
        );
    }
}
