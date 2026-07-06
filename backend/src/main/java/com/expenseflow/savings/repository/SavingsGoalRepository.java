package com.expenseflow.savings.repository;

import com.expenseflow.savings.entity.SavingsGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoalEntity, String>, JpaSpecificationExecutor<SavingsGoalEntity> {
    List<SavingsGoalEntity> findByUserIdAndIsDeletedFalse(String userId);
}
