package com.expenseflow.expense.specification;

import com.expenseflow.expense.domain.entity.ExpenseEntity;
import com.expenseflow.expense.domain.entity.ExpenseParticipantEntity;
import com.expenseflow.expense.domain.valueobject.ExpenseStatus;
import org.springframework.data.jpa.domain.Specification;

public final class ExpenseSpecification {

    private ExpenseSpecification() {}

    public static Specification<ExpenseEntity> notDeleted() {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("isDeleted"), false);
    }

    public static Specification<ExpenseEntity> involvingUser(String userId) {
        return (root, query, criteriaBuilder) -> {
            var isPayer = criteriaBuilder.equal(root.get("paidByUserId"), userId);
            
            // EXISTS subquery to avoid duplicate rows and keep pagination count correct
            var subquery = query.subquery(Integer.class);
            var subRoot = subquery.from(ExpenseParticipantEntity.class);
            subquery.select(criteriaBuilder.literal(1));
            subquery.where(
                criteriaBuilder.and(
                    criteriaBuilder.equal(subRoot.get("expense"), root),
                    criteriaBuilder.equal(subRoot.get("userId"), userId)
                )
            );
            var existsInParticipants = criteriaBuilder.exists(subquery);

            return criteriaBuilder.or(isPayer, existsInParticipants);
        };
    }

    public static Specification<ExpenseEntity> group(String groupId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("groupId"), groupId);
    }

    public static Specification<ExpenseEntity> trip(String tripId) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("tripId"), tripId);
    }

    public static Specification<ExpenseEntity> status(ExpenseStatus status) {
        return (root, query, criteriaBuilder) -> 
            criteriaBuilder.equal(root.get("status"), status);
    }
}
