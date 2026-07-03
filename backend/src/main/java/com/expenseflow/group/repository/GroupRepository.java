package com.expenseflow.group.repository;

import com.expenseflow.group.entity.GroupCode;
import com.expenseflow.group.entity.GroupEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<GroupEntity, String>, JpaSpecificationExecutor<GroupEntity> {

    Optional<GroupEntity> findByIdAndIsDeletedFalse(String id);

    boolean existsByIdAndIsDeletedFalse(String id);

    Optional<GroupEntity> findByGroupCodeAndIsDeletedFalse(GroupCode groupCode);

    boolean existsByGroupCodeAndIsDeletedFalse(GroupCode groupCode);

    @Query("SELECT gm.group FROM GroupMemberEntity gm WHERE gm.user.id = :userId AND gm.status = 'ACTIVE' AND gm.group.isDeleted = false")
    Page<GroupEntity> findActiveGroupsByUserId(@Param("userId") String userId, Pageable pageable);
}
