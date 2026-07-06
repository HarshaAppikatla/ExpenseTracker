package com.expenseflow.budget.repository;

import com.expenseflow.expense.domain.entity.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface BudgetStatisticsRepository extends JpaRepository<ExpenseEntity, String> {

    @Query("SELECT COALESCE(SUM(s.owedAmount), 0.00) FROM ExpenseEntity e JOIN e.splits s, CategoryEntity c " +
           "WHERE s.userId = :userId " +
           "AND c.id = :categoryId " +
           "AND LOWER(e.category) = LOWER(c.name) " +
           "AND e.expenseDate >= CAST(:startDate AS LocalDate) " +
           "AND e.expenseDate <= CAST(:endDate AS LocalDate) " +
           "AND e.isDeleted = false AND s.isDeleted = false")
    BigDecimal calculateSpendsForCategory(
            @Param("userId") String userId,
            @Param("categoryId") String categoryId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COALESCE(SUM(s.owedAmount), 0.00) FROM ExpenseEntity e JOIN e.splits s " +
           "WHERE s.userId = :userId " +
           "AND e.expenseDate >= CAST(:startDate AS LocalDate) " +
           "AND e.expenseDate <= CAST(:endDate AS LocalDate) " +
           "AND e.isDeleted = false AND s.isDeleted = false")
    BigDecimal sumExpensesByUserIdAndDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT c.id, c.name, COALESCE(SUM(s.owedAmount), 0.00), c.color " +
           "FROM ExpenseEntity e JOIN e.splits s, CategoryEntity c " +
           "WHERE s.userId = :userId " +
           "AND LOWER(e.category) = LOWER(c.name) " +
           "AND e.expenseDate >= CAST(:startDate AS LocalDate) " +
           "AND e.expenseDate <= CAST(:endDate AS LocalDate) " +
           "AND e.isDeleted = false AND s.isDeleted = false " +
           "GROUP BY c.id, c.name, c.color " +
           "ORDER BY SUM(s.owedAmount) DESC")
    java.util.List<Object[]> getCategoryBreakdownForDateRange(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
