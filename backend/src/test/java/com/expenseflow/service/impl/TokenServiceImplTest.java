package com.expenseflow.service.impl;

import com.expenseflow.config.SecurityProperties;
import com.expenseflow.dto.auth.LoginContext;
import com.expenseflow.dto.auth.TokenRefreshResponse;
import com.expenseflow.entity.RefreshTokenEntity;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import com.expenseflow.event.security.ReplayDetectedEvent;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.repository.RefreshTokenRepository;
import com.expenseflow.security.JwtService;
import com.expenseflow.security.UserPrincipal;
import com.expenseflow.util.HashUtil;
import com.expenseflow.util.TimeProvider;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtService jwtService;
    @Mock private SecurityProperties securityProperties;
    @Mock private TimeProvider timeProvider;
    @Mock private EntityManager entityManager;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TokenServiceImpl tokenService;

    private LocalDateTime fixedTime;
    private LoginContext fixedContext;

    @BeforeEach
    void setUp() {
        fixedTime = LocalDateTime.of(2026, 6, 28, 12, 0);
        fixedContext = LoginContext.builder()
                .ipAddress("127.0.0.1")
                .userAgent("Mozilla/5.0")
                .requestId("req-123")
                .correlationId("corr-123")
                .loginTime(fixedTime)
                .build();
    }

    // ─── createRefreshToken Tests ─────────────────────────────────────────────

    @Test
    void testCreateRefreshToken_Success() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setRoles(Collections.emptySet());

        // SecurityProperties.Jwt inner class provides refresh expiry days
        SecurityProperties.Jwt jwtProps = mock(SecurityProperties.Jwt.class);
        when(jwtProps.getRefreshTokenExpiryDays()).thenReturn(7);
        when(securityProperties.getJwt()).thenReturn(jwtProps);

        when(timeProvider.now()).thenReturn(fixedTime);
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        RefreshTokenEntity result = tokenService.createRefreshToken(user, fixedContext);

        // Assert
        assertThat(result.getToken()).isNotEmpty();         // hashed token persisted
        assertThat(result.getPlainToken()).isNotEmpty();    // plain token returned to client
        assertThat(result.getTokenFamily()).isNotEmpty();
        assertThat(result.getExpiryDate()).isEqualTo(fixedTime.plusDays(7));
        assertThat(result.getIpAddress()).isEqualTo("127.0.0.1");
        verify(refreshTokenRepository).deleteByUser(user); // old tokens revoked first
        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
    }

    // ─── verifyExpiration Tests ───────────────────────────────────────────────

    @Test
    void testVerifyExpiration_NotExpired_ReturnsToken() {
        // Arrange
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setExpiryDate(fixedTime.plusDays(1));

        when(timeProvider.now()).thenReturn(fixedTime);

        // Act
        RefreshTokenEntity result = tokenService.verifyExpiration(token);

        // Assert
        assertThat(result).isSameAs(token);
    }

    @Test
    void testVerifyExpiration_Expired_ThrowsAndDeletes() {
        // Arrange
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setExpiryDate(fixedTime.minusMinutes(1)); // expired 1 minute ago

        when(timeProvider.now()).thenReturn(fixedTime);

        // Act & Assert
        assertThatThrownBy(() -> tokenService.verifyExpiration(token))
                .isInstanceOf(SecurityHardeningException.class)
                .hasMessageContaining("expired");

        verify(refreshTokenRepository).delete(token);
    }

    // ─── rotateRefreshToken Tests ─────────────────────────────────────────────

    @Test
    void testRotateRefreshToken_ValidToken_RotatesSuccessfully() {
        // Arrange
        String plainToken = UUID.randomUUID().toString();
        String hashedToken = HashUtil.sha256(plainToken);

        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setRoles(Collections.emptySet());

        RefreshTokenEntity activeToken = RefreshTokenEntity.builder()
                .token(hashedToken)
                .tokenFamily("family-uuid")
                .expiryDate(fixedTime.plusDays(1)) // not expired
                .user(user)
                .build();

        when(refreshTokenRepository.findByToken(hashedToken)).thenReturn(Optional.of(activeToken));
        when(timeProvider.now()).thenReturn(fixedTime);

        SecurityProperties.Jwt jwtProps = mock(SecurityProperties.Jwt.class);
        when(jwtProps.getRefreshTokenExpiryDays()).thenReturn(7);
        when(securityProperties.getJwt()).thenReturn(jwtProps);

        when(jwtService.generateAccessToken(any(UserPrincipal.class))).thenReturn("new-access-token");
        when(jwtService.getExpirationMs()).thenReturn(900000L);
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act – method accepts plain string
        TokenRefreshResponse response = tokenService.rotateRefreshToken(plainToken, fixedContext);

        // Assert
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isNotEmpty(); // new plain token
        // Old token was soft-deleted via repository.delete
        verify(refreshTokenRepository).delete(activeToken);
        // New token was saved
        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
    }

    @Test
    void testRotateRefreshToken_ReplayAttack_RevokesAllSessionsAndPublishesEvent() {
        // Arrange – the active token lookup returns empty (soft-delete filter hides it)
        String plainToken = UUID.randomUUID().toString();
        String hashedToken = HashUtil.sha256(plainToken);

        when(refreshTokenRepository.findByToken(hashedToken)).thenReturn(Optional.empty());

        // Hibernate Session mock for filter bypass
        Session session = mock(Session.class);
        when(entityManager.unwrap(Session.class)).thenReturn(session);

        // When filter is disabled, the deleted token IS found
        UserEntity user = new UserEntity();
        user.setId("user-uuid");
        user.setEmail("attacker@example.com");

        RefreshTokenEntity deletedToken = RefreshTokenEntity.builder()
                .token(hashedToken)
                .tokenFamily("family-uuid")
                .user(user)
                .build();

        // After disableFilter is called, re-query returns the deleted record
        // We use doAnswer to simulate the filter toggle side-effect:
        // First call (active filter on) → empty, second call (filter off) → deleted token
        when(refreshTokenRepository.findByToken(hashedToken))
                .thenReturn(Optional.empty())       // first call: active (filtered)
                .thenReturn(Optional.of(deletedToken)); // second call: after disableFilter

        // Act & Assert
        assertThatThrownBy(() -> tokenService.rotateRefreshToken(plainToken, fixedContext))
                .isInstanceOf(SecurityHardeningException.class)
                .hasMessageContaining("Session revoked due to token replay detection");

        // Verify filter was toggled correctly
        verify(session).disableFilter("deletedFilter");
        verify(session).enableFilter("deletedFilter");

        // All user sessions revoked
        verify(refreshTokenRepository).deleteByUser(user);

        // Replay event published
        verify(eventPublisher).publishEvent(any(ReplayDetectedEvent.class));
    }

    @Test
    void testRotateRefreshToken_TotallyUnknownToken_ThrowsInvalidException() {
        // Arrange – token not found even after disabling the soft-delete filter
        String plainToken = UUID.randomUUID().toString();
        String hashedToken = HashUtil.sha256(plainToken);

        when(refreshTokenRepository.findByToken(hashedToken)).thenReturn(Optional.empty());

        Session session = mock(Session.class);
        when(entityManager.unwrap(Session.class)).thenReturn(session);

        // Both calls return empty (token never existed)
        when(refreshTokenRepository.findByToken(hashedToken))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> tokenService.rotateRefreshToken(plainToken, fixedContext))
                .isInstanceOf(SecurityHardeningException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(session).disableFilter("deletedFilter");
        verify(session).enableFilter("deletedFilter");
        // No event published, no sessions revoked
        verifyNoInteractions(eventPublisher);
        verify(refreshTokenRepository, never()).deleteByUser(any());
    }
}
