package com.expenseflow.group.repository;

import com.expenseflow.group.entity.GroupMemberEntity;
import com.expenseflow.group.entity.GroupMemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMemberEntity, String> {

    Optional<GroupMemberEntity> findByGroupIdAndUserIdAndIsDeletedFalse(String groupId, String userId);

    Optional<GroupMemberEntity> findByGroupIdAndUserIdAndStatusAndIsDeletedFalse(String groupId, String userId, GroupMemberStatus status);

    boolean existsByGroupIdAndUserIdAndStatusAndIsDeletedFalse(String groupId, String userId, GroupMemberStatus status);

    int countByGroupIdAndStatusAndIsDeletedFalse(String groupId, GroupMemberStatus status);

    @Query("SELECT gm FROM GroupMemberEntity gm JOIN FETCH gm.user WHERE gm.group.id = :groupId AND gm.status = :status AND gm.isDeleted = false")
    Page<GroupMemberEntity> findActiveMembersByGroupId(
            @Param("groupId") String groupId, 
            @Param("status") GroupMemberStatus status, 
            Pageable pageable
    );

    @Query("SELECT gm FROM GroupMemberEntity gm JOIN FETCH gm.user WHERE gm.group.id = :groupId AND gm.status = :status AND gm.isDeleted = false AND " +
           "LOWER(gm.user.fullName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<GroupMemberEntity> searchActiveMembersByGroupIdAndName(
            @Param("groupId") String groupId, 
            @Param("status") GroupMemberStatus status, 
            @Param("search") String search, 
            Pageable pageable
    );
}
