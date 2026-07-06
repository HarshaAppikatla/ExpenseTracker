package com.expenseflow.service;

import com.expenseflow.dto.auth.LoginContext;
import com.expenseflow.dto.auth.TokenRefreshResponse;
import com.expenseflow.entity.RefreshTokenEntity;
import com.expenseflow.entity.UserEntity;

import java.util.Optional;

public interface TokenService {
    RefreshTokenEntity createRefreshToken(UserEntity user, LoginContext loginContext);
    RefreshTokenEntity verifyExpiration(RefreshTokenEntity token);
    Optional<RefreshTokenEntity> findByToken(String token);
    void revokeToken(String token);
    void revokeAllUserTokens(UserEntity user);
    TokenRefreshResponse rotateRefreshToken(String tokenString, LoginContext loginContext);
}
