package com.expenseflow.repository;

import com.expenseflow.entity.PasswordHistoryEntity;
import com.expenseflow.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistoryEntity, String> {
    List<PasswordHistoryEntity> findByUserOrderByCreatedAtDesc(UserEntity user);
    long countByUser(UserEntity user);
}
