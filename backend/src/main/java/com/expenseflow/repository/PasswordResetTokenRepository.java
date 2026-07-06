package com.expenseflow.repository;

import com.expenseflow.entity.PasswordResetTokenEntity;
import com.expenseflow.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, String> {
    Optional<PasswordResetTokenEntity> findByToken(String token);
    void deleteByUser(UserEntity user);
}
