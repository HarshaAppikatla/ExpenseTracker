package com.expenseflow.trip.domain.specification;

import com.expenseflow.trip.domain.entity.TripEntity;
import org.springframework.data.jpa.domain.Specification;

public class TripSpecification {

    public static Specification<TripEntity> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), cb.literal(false));
    }

    public static Specification<TripEntity> hasGroupId(String groupId) {
        return (root, query, cb) -> cb.equal(root.get("groupId"), cb.literal(groupId));
    }

    public static Specification<TripEntity> titleContains(String title) {
        return (root, query, cb) -> {
            if (title == null || title.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + title.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("title")), cb.literal(pattern));
        };
    }
}
