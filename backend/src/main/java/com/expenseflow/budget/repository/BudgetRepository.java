package com.expenseflow.budget.repository;

import com.expenseflow.budget.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<BudgetEntity, String>, JpaSpecificationExecutor<BudgetEntity> {
    Optional<BudgetEntity> findByUserIdAndCategoryIdAndBudgetYearAndBudgetMonthAndIsDeletedFalse(
            String userId, String categoryId, int budgetYear, int budgetMonth);

    List<BudgetEntity> findByUserIdAndBudgetYearAndBudgetMonthAndIsDeletedFalse(
            String userId, int budgetYear, int budgetMonth);

    List<BudgetEntity> findByUserIdAndCategoryIdAndIsDeletedFalse(String userId, String categoryId);
}
