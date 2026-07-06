package com.expenseflow.repository;

import com.expenseflow.entity.RefreshTokenEntity;
import com.expenseflow.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {
    Optional<RefreshTokenEntity> findByToken(String token);
    void deleteByUser(UserEntity user);
}
