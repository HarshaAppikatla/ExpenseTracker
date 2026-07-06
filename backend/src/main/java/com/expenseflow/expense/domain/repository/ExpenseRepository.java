package com.expenseflow.expense.domain.repository;

import com.expenseflow.expense.domain.entity.ExpenseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<ExpenseEntity, String>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<ExpenseEntity> {

    Optional<ExpenseEntity> findByIdAndIsDeletedFalse(String id);

    Page<ExpenseEntity> findByGroupIdAndIsDeletedFalse(String groupId, Pageable pageable);

    Page<ExpenseEntity> findByGroupIdAndTripIdAndIsDeletedFalse(String groupId, String tripId, Pageable pageable);

    java.util.List<ExpenseEntity> findByGroupIdAndStatusAndIsDeletedFalse(String groupId, com.expenseflow.expense.domain.valueobject.ExpenseStatus status);

    java.util.List<ExpenseEntity> findByGroupIdAndTripIdAndStatusAndIsDeletedFalse(String groupId, String tripId, com.expenseflow.expense.domain.valueobject.ExpenseStatus status);


    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(s.owedAmount), 0) FROM ExpenseEntity e JOIN e.splits s WHERE s.userId = :userId AND e.isDeleted = false AND s.isDeleted = false")
    java.math.BigDecimal sumExpensesByUserId(@org.springframework.data.repository.query.Param("userId") String userId);

    @org.springframework.data.jpa.repository.Query("SELECT e.category as category, COALESCE(SUM(s.owedAmount), 0) as amount " +
           "FROM ExpenseEntity e JOIN e.splits s WHERE s.userId = :userId AND e.isDeleted = false AND s.isDeleted = false " +
           "GROUP BY e.category")
    java.util.List<Object[]> getCategoryBreakdownRaw(@org.springframework.data.repository.query.Param("userId") String userId);

    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT e FROM ExpenseEntity e LEFT JOIN e.participants p WHERE (e.paidByUserId = :userId OR p.userId = :userId) AND e.isDeleted = false")
    java.util.List<ExpenseEntity> findAllUserExpenses(@org.springframework.data.repository.query.Param("userId") String userId);
}
