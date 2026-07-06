package com.expenseflow.repository;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.VerificationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationTokenEntity, String> {
    Optional<VerificationTokenEntity> findByToken(String token);
    void deleteByUser(UserEntity user);
}
