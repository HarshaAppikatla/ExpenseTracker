package com.expenseflow.integration;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.dto.auth.*;
import com.expenseflow.dto.user.UserDto;
import com.expenseflow.entity.PasswordResetTokenEntity;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.VerificationTokenEntity;
import com.expenseflow.repository.PasswordResetTokenRepository;
import com.expenseflow.repository.RefreshTokenRepository;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.repository.VerificationTokenRepository;
import com.expenseflow.security.JwtService;
import com.expenseflow.service.AuthService;
import com.expenseflow.service.TokenService;
import com.expenseflow.util.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuthenticationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtService jwtService;

    private LoginContext loginContext;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAllInBatch();
        passwordResetTokenRepository.deleteAllInBatch();
        verificationTokenRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        loginContext = LoginContext.builder()
                .ipAddress("127.0.0.1")
                .userAgent("Mozilla/5.0")
                .browser("Chrome")
                .operatingSystem("Windows")
                .deviceName("Desktop")
                .build();
    }

    @Test
    void testFullAuthenticationLifecycle() {
        // 1. User Registration
        RegisterRequest registerReq = new RegisterRequest("Integrator", "integration@example.com", "Password123!");
        UserDto registeredUser = authService.register(registerReq);

        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getEmail()).isEqualTo("integration@example.com");

        Optional<UserEntity> userOpt = userRepository.findByEmail("integration@example.com");
        assertThat(userOpt).isPresent();
        assertThat(userOpt.get().getStatus().name()).isEqualTo("PENDING_VERIFICATION");

        // Find verification token
        List<VerificationTokenEntity> vTokens = verificationTokenRepository.findAll();
        assertThat(vTokens).hasSize(1);
        String vHashedToken = vTokens.get(0).getToken();

        // Since we can't read the plain token directly from database (it's SHA-256 hashed),
        // we create a custom verification token for this test where we know the plain token!
        verificationTokenRepository.deleteAll();
        String plainVerifyToken = "my-plain-verify-token";
        VerificationTokenEntity customVToken = VerificationTokenEntity.builder()
                .token(HashUtil.sha256(plainVerifyToken))
                .expiryDate(java.time.LocalDateTime.now().plusHours(24))
                .user(userOpt.get())
                .build();
        verificationTokenRepository.save(customVToken);

        // 2. Verify Email
        authService.verifyEmail(plainVerifyToken);
        Optional<UserEntity> verifiedUserOpt = userRepository.findByEmail("integration@example.com");
        assertThat(verifiedUserOpt).isPresent();
        assertThat(verifiedUserOpt.get().getStatus().name()).isEqualTo("ACTIVE");

        // 3. User Login
        LoginRequest loginReq = new LoginRequest("integration@example.com", "Password123!");
        LoginResponse loginResp = authService.login(loginReq, loginContext);

        assertThat(loginResp.getAccessToken()).isNotEmpty();
        assertThat(loginResp.getRefreshToken()).isNotEmpty();
        assertThat(jwtService.validateToken(loginResp.getAccessToken())).isTrue();
        assertThat(jwtService.getEmailFromToken(loginResp.getAccessToken())).isEqualTo("integration@example.com");

        String firstRefreshToken = loginResp.getRefreshToken();

        // 4. Token Refresh Rotation
        TokenRefreshRequest refreshReq = new TokenRefreshRequest(firstRefreshToken);
        TokenRefreshResponse refreshResp = tokenService.rotateRefreshToken(firstRefreshToken, loginContext);

        assertThat(refreshResp.getAccessToken()).isNotEmpty();
        assertThat(refreshResp.getRefreshToken()).isNotEmpty();
        assertThat(refreshResp.getRefreshToken()).isNotEqualTo(firstRefreshToken); // Rotation occurred!

        String secondRefreshToken = refreshResp.getRefreshToken();

        // 5. User Logout
        authService.logout(secondRefreshToken);

        // Assert: Rotate with same second token fails (since it's revoked)
        // And first token is also fully revoked
        Optional<UserEntity> postLogoutUser = userRepository.findByEmail("integration@example.com");
        assertThat(postLogoutUser).isPresent();
        assertThat(postLogoutUser.get().getLastLogoutAt()).isNotNull();

        // 6. Forgot Password
        authService.forgotPassword("integration@example.com");
        List<PasswordResetTokenEntity> resetTokens = passwordResetTokenRepository.findAll();
        assertThat(resetTokens).hasSize(1);
        
        // Setup known password reset token
        passwordResetTokenRepository.deleteAll();
        String plainResetToken = "my-plain-reset-token";
        PasswordResetTokenEntity customResetToken = PasswordResetTokenEntity.builder()
                .token(HashUtil.sha256(plainResetToken))
                .expiryDate(java.time.LocalDateTime.now().plusMinutes(15))
                .user(verifiedUserOpt.get())
                .build();
        passwordResetTokenRepository.save(customResetToken);

        // 7. Reset Password
        ResetPasswordRequest resetReq = new ResetPasswordRequest(plainResetToken, "NewSecurePassword123!");
        authService.resetPassword(resetReq);

        // 8. Login with new Password
        LoginRequest loginWithNewReq = new LoginRequest("integration@example.com", "NewSecurePassword123!");
        LoginResponse loginWithNewResp = authService.login(loginWithNewReq, loginContext);

        assertThat(loginWithNewResp.getAccessToken()).isNotEmpty();
    }
}
