package com.expenseflow.income.specification;

import com.expenseflow.income.entity.IncomeEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class IncomeSpecification {

    public static Specification<IncomeEntity> hasUserId(String userId) {
        return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
    }

    public static Specification<IncomeEntity> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), false);
    }

    public static Specification<IncomeEntity> searchSource(String query) {
        return (root, query1, cb) -> {
            if (query == null || query.trim().isEmpty()) return null;
            return cb.like(cb.lower(root.get("source")), "%" + query.trim().toLowerCase() + "%");
        };
    }

    public static Specification<IncomeEntity> amountGreaterThanOrEqual(BigDecimal minAmount) {
        return (root, query, cb) -> {
            if (minAmount == null) return null;
            return cb.greaterThanOrEqualTo(root.get("amount"), minAmount);
        };
    }

    public static Specification<IncomeEntity> amountLessThanOrEqual(BigDecimal maxAmount) {
        return (root, query, cb) -> {
            if (maxAmount == null) return null;
            return cb.lessThanOrEqualTo(root.get("amount"), maxAmount);
        };
    }

    public static Specification<IncomeEntity> dateAfterOrEqual(LocalDateTime fromDate) {
        return (root, query, cb) -> {
            if (fromDate == null) return null;
            return cb.greaterThanOrEqualTo(root.get("incomeDate"), fromDate);
        };
    }

    public static Specification<IncomeEntity> dateBeforeOrEqual(LocalDateTime toDate) {
        return (root, query, cb) -> {
            if (toDate == null) return null;
            return cb.lessThanOrEqualTo(root.get("incomeDate"), toDate);
        };
    }

    public static Specification<IncomeEntity> hasDescription(String desc) {
        return (root, query1, cb) -> {
            if (desc == null || desc.trim().isEmpty()) return null;
            return cb.like(cb.lower(root.get("description")), "%" + desc.trim().toLowerCase() + "%");
        };
    }
}
