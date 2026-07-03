package com.expenseflow.group.repository;

import com.expenseflow.group.entity.GroupActivityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupActivityRepository extends JpaRepository<GroupActivityEntity, String> {

    Page<GroupActivityEntity> findByGroupIdOrderByCreatedAtDesc(String groupId, Pageable pageable);
}
