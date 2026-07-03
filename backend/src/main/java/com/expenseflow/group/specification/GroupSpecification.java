package com.expenseflow.group.specification;

import com.expenseflow.group.entity.GroupEntity;
import com.expenseflow.group.entity.GroupMemberEntity;
import com.expenseflow.group.entity.GroupMemberStatus;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class GroupSpecification {

    public static Specification<GroupEntity> isNotDeleted() {
        return (root, query, cb) -> cb.equal(root.get("isDeleted"), cb.literal(false));
    }

    public static Specification<GroupEntity> isMember(String userId) {
        return (root, query, cb) -> {
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<GroupMemberEntity> memberRoot = subquery.from(GroupMemberEntity.class);
            subquery.select(cb.literal(1L))
                .where(
                    cb.equal(memberRoot.get("group").get("id"), root.get("id")),
                    cb.equal(memberRoot.get("user").get("id"), userId),
                    cb.equal(memberRoot.get("status"), GroupMemberStatus.ACTIVE),
                    cb.equal(memberRoot.get("isDeleted"), cb.literal(false))
                );
            return cb.exists(subquery);
        };
    }

    public static Specification<GroupEntity> nameOrDescriptionContains(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                cb.like(cb.lower(root.get("name")), cb.literal(pattern)),
                cb.like(cb.lower(root.get("description")), cb.literal(pattern))
            );
        };
    }
}
