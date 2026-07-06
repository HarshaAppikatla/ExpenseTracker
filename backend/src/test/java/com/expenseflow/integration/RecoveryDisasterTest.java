package com.expenseflow.integration;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.dto.auth.ResetPasswordRequest;
import com.expenseflow.entity.PasswordResetTokenEntity;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.repository.PasswordResetTokenRepository;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.service.AuthService;
import com.expenseflow.util.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class RecoveryDisasterTest extends BaseIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        passwordResetTokenRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        testUser = UserEntity.builder()
                .fullName("Disaster Recovery User")
                .email("recovery@example.com")
                .password("Password123!")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .build();
        userRepository.save(testUser);
    }

    @Test
    void testMultiplePasswordResetRequests_OnlyLatestTokenIsValid() {
        // First Request
        authService.forgotPassword("recovery@example.com");
        List<PasswordResetTokenEntity> tokensAfterFirst = passwordResetTokenRepository.findAll();
        assertThat(tokensAfterFirst).hasSize(1);
        String firstHashedToken = tokensAfterFirst.get(0).getToken();

        // Second Request
        authService.forgotPassword("recovery@example.com");
        List<PasswordResetTokenEntity> tokensAfterSecond = passwordResetTokenRepository.findAll();
        
        // Assert: Old token is pruned, only 1 active reset token exists
        assertThat(tokensAfterSecond).hasSize(1);
        String secondHashedToken = tokensAfterSecond.get(0).getToken();
        assertThat(firstHashedToken).isNotEqualTo(secondHashedToken);
    }

    @Test
    void testExpiredResetToken_ThrowsException() {
        // Create an expired reset token
        String plainToken = UUID.randomUUID().toString();
        String hashedToken = HashUtil.sha256(plainToken);

        PasswordResetTokenEntity expiredToken = PasswordResetTokenEntity.builder()
                .token(hashedToken)
                .expiryDate(LocalDateTime.now().minusMinutes(1)) // Expired 1 minute ago
                .user(testUser)
                .build();
        passwordResetTokenRepository.save(expiredToken);

        ResetPasswordRequest request = new ResetPasswordRequest(plainToken, "NewSecurePassword123!");

        // Assert: Resets with expired tokens fail
        assertThatThrownBy(() -> authService.resetPassword(request))
                .isInstanceOf(SecurityHardeningException.class)
                .hasMessageContaining("expired");
    }
}
