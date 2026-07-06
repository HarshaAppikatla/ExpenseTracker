package com.expenseflow.service.impl;

import com.expenseflow.config.SecurityProperties;
import com.expenseflow.dto.auth.LoginContext;
import com.expenseflow.dto.auth.LoginRequest;
import com.expenseflow.dto.auth.LoginResponse;
import com.expenseflow.dto.auth.RegisterRequest;
import com.expenseflow.entity.RefreshTokenEntity;
import com.expenseflow.entity.RoleEntity;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import com.expenseflow.event.authentication.LoginFailureEvent;
import com.expenseflow.event.authentication.LoginSuccessEvent;
import com.expenseflow.event.authentication.UserRegisteredEvent;
import com.expenseflow.event.security.AccountLockedEvent;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.mapper.user.UserMapper;
import com.expenseflow.repository.PasswordHistoryRepository;
import com.expenseflow.repository.PasswordResetTokenRepository;
import com.expenseflow.repository.RoleRepository;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.repository.VerificationTokenRepository;
import com.expenseflow.security.JwtService;
import com.expenseflow.security.UserPrincipal;
import com.expenseflow.service.EmailService;
import com.expenseflow.service.TokenService;
import com.expenseflow.util.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private VerificationTokenRepository verificationTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordHistoryRepository passwordHistoryRepository;
    @Mock private TokenService tokenService;
    @Mock private EmailService emailService;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserMapper userMapper;
    @Mock private SecurityProperties securityProperties;
    @Mock private TimeProvider timeProvider;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuthServiceImpl authService;

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

    // ─── Registration Tests ───────────────────────────────────────────────────

    @Test
    void testRegister_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "Password123!");
        RoleEntity userRole = new RoleEntity();
        userRole.setName("ROLE_USER");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed-pass");
        when(timeProvider.now()).thenReturn(fixedTime);

        // SecurityProperties.Tokens inner class
        SecurityProperties.Tokens tokenProps = mock(SecurityProperties.Tokens.class);
        when(tokenProps.getVerificationExpiryHours()).thenReturn(24);
        when(securityProperties.getTokens()).thenReturn(tokenProps);

        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        authService.register(request);

        // Assert
        verify(userRepository).save(any(UserEntity.class));
        verify(verificationTokenRepository).save(any());
        verify(emailService).sendVerificationEmail(eq("test@example.com"), anyString());
        verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsException() {
        // Arrange
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "Password123!");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already registered");
    }

    // ─── Login Tests ──────────────────────────────────────────────────────────

    @Test
    void testLogin_Success() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "Password123!");

        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setPassword("hashed-pass");
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(0);
        user.setRoles(Collections.emptySet());

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashed-pass")).thenReturn(true);
        when(timeProvider.now()).thenReturn(fixedTime);

        // tokenService.createRefreshToken returns a RefreshTokenEntity
        RefreshTokenEntity tokenEntity = RefreshTokenEntity.builder()
                .token("hashed-token")
                .plainToken("plain-refresh-token")
                .tokenFamily("family-uuid")
                .expiryDate(fixedTime.plusDays(7))
                .build();
        when(tokenService.createRefreshToken(eq(user), any(LoginContext.class))).thenReturn(tokenEntity);
        when(jwtService.generateAccessToken(any(UserPrincipal.class))).thenReturn("access-token");
        when(jwtService.getExpirationMs()).thenReturn(900000L);

        // Act
        LoginResponse response = authService.login(request, fixedContext);

        // Assert
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("plain-refresh-token");
        verify(eventPublisher).publishEvent(any(LoginSuccessEvent.class));
        assertThat(user.getFailedLoginAttempts()).isZero();
    }

    @Test
    void testLogin_EmailNotVerified_ThrowsException() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "Password123!");
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setStatus(UserStatus.PENDING_VERIFICATION);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(timeProvider.now()).thenReturn(fixedTime);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request, fixedContext))
                .isInstanceOf(SecurityHardeningException.class)
                .hasMessageContaining("verify your email");
        verify(eventPublisher).publishEvent(any(LoginFailureEvent.class));
    }

    @Test
    void testLogin_WrongPassword_IncrementsAttemptsAndSetsDelay() {
        // Arrange – user has had 1 previous failure; this will be the 2nd
        LoginRequest request = new LoginRequest("test@example.com", "WrongPassword");
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setPassword("hashed-pass");
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(1);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hashed-pass")).thenReturn(false);
        when(timeProvider.now()).thenReturn(fixedTime);

        // Lock config: only getLock() is called inside handleFailedLoginAttempt()
        // (getLogin() is never reached because user.getNextAllowedLoginAt() == null)
        SecurityProperties.Lock lockProps = mock(SecurityProperties.Lock.class);
        when(lockProps.getDurations()).thenReturn(List.of(30L, 120L, 720L, 1440L));
        when(securityProperties.getLock()).thenReturn(lockProps);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request, fixedContext))
                .isInstanceOf(SecurityHardeningException.class)
                .hasMessageContaining("Invalid email or password");

        // After 2nd failure: attempt count=2, delay=2s
        assertThat(user.getFailedLoginAttempts()).isEqualTo(2);
        assertThat(user.getNextAllowedLoginAt()).isEqualTo(fixedTime.plusSeconds(2));
        verify(eventPublisher).publishEvent(any(LoginFailureEvent.class));
    }

    @Test
    void testLogin_FifthFailure_LocksAccount() {
        // Arrange – user has had 4 previous failures; 5th triggers lockout
        LoginRequest request = new LoginRequest("test@example.com", "WrongPassword");
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setPassword("hashed-pass");
        user.setStatus(UserStatus.ACTIVE);
        user.setFailedLoginAttempts(4);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword", "hashed-pass")).thenReturn(false);
        when(timeProvider.now()).thenReturn(fixedTime);

        // Lock config: getLock().getDurations() is called inside handleFailedLoginAttempt()
        // for the 5th attempt to determine the 30-minute lockout duration
        SecurityProperties.Lock lockProps = mock(SecurityProperties.Lock.class);
        when(lockProps.getDurations()).thenReturn(List.of(30L, 120L, 720L, 1440L));
        when(securityProperties.getLock()).thenReturn(lockProps);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(request, fixedContext))
                .isInstanceOf(SecurityHardeningException.class)
                .hasMessageContaining("Invalid email or password");

        assertThat(user.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
        assertThat(user.getAccountLockedUntil()).isEqualTo(fixedTime.plusMinutes(30));
        verify(eventPublisher).publishEvent(any(AccountLockedEvent.class));
    }
}
