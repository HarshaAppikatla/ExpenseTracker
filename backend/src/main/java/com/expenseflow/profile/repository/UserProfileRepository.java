package com.expenseflow.profile.repository;

import com.expenseflow.profile.entity.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, String> {
    Optional<UserProfileEntity> findByUserId(String userId);
    Optional<UserProfileEntity> findByUserEmail(String email);
}
