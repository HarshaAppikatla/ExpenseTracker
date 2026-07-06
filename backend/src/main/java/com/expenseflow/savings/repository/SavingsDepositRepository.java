package com.expenseflow.savings.repository;

import com.expenseflow.savings.entity.SavingsDepositEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SavingsDepositRepository extends JpaRepository<SavingsDepositEntity, String> {
    List<SavingsDepositEntity> findBySavingsGoalId(String goalId);

    @Query("SELECT COALESCE(SUM(d.amount), 0.00) FROM SavingsDepositEntity d WHERE d.savingsGoal.id = :goalId")
    BigDecimal calculateSumByGoalId(@Param("goalId") String goalId);
}
