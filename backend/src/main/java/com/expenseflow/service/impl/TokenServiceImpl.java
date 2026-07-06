package com.expenseflow.service.impl;

import com.expenseflow.config.SecurityProperties;
import com.expenseflow.dto.auth.LoginContext;
import com.expenseflow.dto.auth.TokenRefreshResponse;
import com.expenseflow.entity.RefreshTokenEntity;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.event.security.ReplayDetectedEvent;
import com.expenseflow.repository.RefreshTokenRepository;
import com.expenseflow.security.JwtService;
import com.expenseflow.security.UserPrincipal;
import com.expenseflow.service.TokenService;
import com.expenseflow.util.HashUtil;
import com.expenseflow.util.TimeProvider;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final SecurityProperties securityProperties;
    private final TimeProvider timeProvider;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public RefreshTokenEntity createRefreshToken(UserEntity user, LoginContext loginContext) {
        // Revoke any existing tokens first to avoid multiple active sessions per user
        refreshTokenRepository.deleteByUser(user);

        String plainToken = UUID.randomUUID().toString();
        String hashedToken = HashUtil.sha256(plainToken);
        String family = UUID.randomUUID().toString();

        int expiryDays = securityProperties.getJwt().getRefreshTokenExpiryDays();

        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
                .user(user)
                .token(hashedToken)
                .tokenFamily(family)
                .expiryDate(timeProvider.now().plusDays(expiryDays))
                .ipAddress(loginContext.getIpAddress())
                .userAgent(loginContext.getUserAgent())
                .deviceName(loginContext.getDeviceName())
                .browser(loginContext.getBrowser())
                .os(loginContext.getOperatingSystem())
                .build();

        // Assign plain token transient field to return it to the client
        refreshToken.setPlainToken(plainToken);

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshTokenEntity verifyExpiration(RefreshTokenEntity token) {
        if (token.getExpiryDate().isBefore(timeProvider.now())) {
            refreshTokenRepository.delete(token);
            throw new SecurityHardeningException("Refresh token was expired. Please make a new login request.", "AUTH_004");
        }
        return token;
    }

    @Override
    public Optional<RefreshTokenEntity> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(UserEntity user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Override
    @Transactional
    public TokenRefreshResponse rotateRefreshToken(String tokenString, LoginContext loginContext) {
        String hashedToken = HashUtil.sha256(tokenString);

        // Try to find the token in active state
        Optional<RefreshTokenEntity> tokenOpt = refreshTokenRepository.findByToken(hashedToken);

        if (tokenOpt.isEmpty()) {
            // Token not found in active session, check if it was previously soft-deleted (indicates replay attack!)
            Session session = entityManager.unwrap(Session.class);
            // Disable soft-delete filter temporarily to search for deleted records
            session.disableFilter("deletedFilter");
            Optional<RefreshTokenEntity> replayedTokenOpt = refreshTokenRepository.findByToken(hashedToken);
            session.enableFilter("deletedFilter");

            if (replayedTokenOpt.isPresent()) {
                RefreshTokenEntity replayedToken = replayedTokenOpt.get();
                // Revoke all tokens sharing the same family
                log.warn("Replay attack detected for refresh token! Revoking all sessions in token family: {}", replayedToken.getTokenFamily());
                refreshTokenRepository.deleteByUser(replayedToken.getUser()); // Revoke all sessions

                // Publish security replay event
                eventPublisher.publishEvent(new ReplayDetectedEvent(this, replayedToken.getUser().getId(), replayedToken.getTokenFamily(), loginContext));

                throw new SecurityHardeningException("Session revoked due to token replay detection.", "AUTH_005");
            }

            throw new SecurityHardeningException("Invalid refresh token", "AUTH_001");
        }

        RefreshTokenEntity oldToken = tokenOpt.get();

        // Verify expiration
        verifyExpiration(oldToken);

        UserEntity user = oldToken.getUser();
        String family = oldToken.getTokenFamily();

        // Delete the old refresh token (soft-delete)
        refreshTokenRepository.delete(oldToken);

        // Generate new rotated token preserving the family ID
        String plainToken = UUID.randomUUID().toString();
        String hashedNewToken = HashUtil.sha256(plainToken);
        int expiryDays = securityProperties.getJwt().getRefreshTokenExpiryDays();

        RefreshTokenEntity newToken = RefreshTokenEntity.builder()
                .user(user)
                .token(hashedNewToken)
                .tokenFamily(family)
                .expiryDate(timeProvider.now().plusDays(expiryDays))
                .ipAddress(loginContext.getIpAddress())
                .userAgent(loginContext.getUserAgent())
                .deviceName(loginContext.getDeviceName())
                .browser(loginContext.getBrowser())
                .os(loginContext.getOperatingSystem())
                .build();

        refreshTokenRepository.save(newToken);

        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);

        return TokenRefreshResponse.builder()
                .accessToken(accessToken)
                .refreshToken(plainToken) // Return plain token to client
                .expiresIn(jwtService.getExpirationMs())
                .build();
    }
}
