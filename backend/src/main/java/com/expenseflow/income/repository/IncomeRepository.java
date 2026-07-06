package com.expenseflow.income.repository;

import com.expenseflow.income.entity.IncomeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface IncomeRepository extends JpaRepository<IncomeEntity, String>, JpaSpecificationExecutor<IncomeEntity> {
    Page<IncomeEntity> findByUserIdAndIsDeletedFalse(String userId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(i.amount), 0) FROM IncomeEntity i WHERE i.user.id = :userId AND i.isDeleted = false")
    java.math.BigDecimal sumIncomeByUserId(@org.springframework.data.repository.query.Param("userId") String userId);

    @org.springframework.data.jpa.repository.Query("SELECT COALESCE(SUM(i.amount), 0.00) FROM IncomeEntity i " +
           "WHERE i.user.id = :userId " +
           "AND i.incomeDate >= :startDate " +
           "AND i.incomeDate <= :endDate " +
           "AND i.isDeleted = false")
    java.math.BigDecimal sumIncomeByUserIdAndDateRange(
            @org.springframework.data.repository.query.Param("userId") String userId,
            @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
            @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate
    );
}
