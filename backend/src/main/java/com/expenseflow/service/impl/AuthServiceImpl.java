package com.expenseflow.service.impl;

import com.expenseflow.config.SecurityProperties;
import com.expenseflow.dto.auth.LoginContext;
import com.expenseflow.dto.auth.LoginRequest;
import com.expenseflow.dto.auth.LoginResponse;
import com.expenseflow.dto.auth.RegisterRequest;
import com.expenseflow.dto.auth.ResetPasswordRequest;
import com.expenseflow.dto.user.UserDto;
import com.expenseflow.entity.*;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.event.authentication.LoginFailureEvent;
import com.expenseflow.event.authentication.LoginSuccessEvent;
import com.expenseflow.event.authentication.UserRegisteredEvent;
import com.expenseflow.event.security.AccountLockedEvent;
import com.expenseflow.mapper.user.UserMapper;
import com.expenseflow.repository.*;
import com.expenseflow.security.JwtService;
import com.expenseflow.security.UserPrincipal;
import com.expenseflow.service.AuthService;
import com.expenseflow.service.EmailService;
import com.expenseflow.service.TokenService;
import com.expenseflow.util.HashUtil;
import com.expenseflow.util.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final SecurityProperties securityProperties;
    private final TimeProvider timeProvider;
    private final ApplicationEventPublisher eventPublisher;

    private static final String DEFAULT_ROLE_NAME = "ROLE_USER";

    @Override
    @Transactional
    public UserDto register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email {} is already registered", request.getEmail());
            throw new IllegalArgumentException("Email already registered");
        }

        RoleEntity defaultRole = roleRepository.findByName(DEFAULT_ROLE_NAME)
                .orElseThrow(() -> new RuntimeException("Default system role " + DEFAULT_ROLE_NAME + " not found"));

        UserEntity user = UserEntity.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.PENDING_VERIFICATION)
                .loginProvider("LOCAL")
                .roles(Collections.singleton(defaultRole))
                .build();

        UserEntity savedUser = userRepository.save(user);

        // Generate email verification token (24-hour expiry)
        String plainToken = UUID.randomUUID().toString();
        String hashedToken = HashUtil.sha256(plainToken);
        
        int verificationHours = securityProperties.getTokens().getVerificationExpiryHours();
        VerificationTokenEntity verificationToken = VerificationTokenEntity.builder()
                .token(hashedToken)
                .expiryDate(timeProvider.now().plusHours(verificationHours))
                .user(savedUser)
                .build();
        verificationTokenRepository.save(verificationToken);

        // Trigger verification email link in console
        emailService.sendVerificationEmail(savedUser.getEmail(), plainToken);

        // Publish domain event
        eventPublisher.publishEvent(new UserRegisteredEvent(this, savedUser, plainToken));

        log.info("User registered successfully. ID: {}", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, LoginContext loginContext) {
        log.info("Processing login request for: {}", request.getEmail());

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    // Generic error to prevent email enumeration
                    eventPublisher.publishEvent(new LoginFailureEvent(this, request.getEmail(), loginContext, "Invalid email or password"));
                    return new SecurityHardeningException("Invalid email or password", "AUTH_001");
                });

        LocalDateTime now = timeProvider.now();

        // 1. Process progressive login attempt counter decay
        if (user.getNextAllowedLoginAt() != null) {
            long minutesPast = Duration.between(user.getNextAllowedLoginAt(), now).toMinutes();
            if (minutesPast > 0) {
                int decayInterval = securityProperties.getLogin().getDecayIntervalMinutes();
                int decayAmount = (int) (minutesPast / decayInterval);
                if (decayAmount > 0) {
                    int newAttempts = Math.max(0, user.getFailedLoginAttempts() - decayAmount);
                    user.setFailedLoginAttempts(newAttempts);
                    if (newAttempts == 0) {
                        user.setNextAllowedLoginAt(null);
                    } else {
                        user.setNextAllowedLoginAt(now);
                    }
                    userRepository.save(user);
                }
            }
        }

        // 2. Validate current progressive delay / lockout limits
        if (user.getNextAllowedLoginAt() != null && user.getNextAllowedLoginAt().isAfter(now)) {
            long retryAfterSeconds = Duration.between(now, user.getNextAllowedLoginAt()).toSeconds();
            eventPublisher.publishEvent(new LoginFailureEvent(this, request.getEmail(), loginContext, "Progressive lock/delay is active"));
            
            if (user.getFailedLoginAttempts() >= securityProperties.getLogin().getMaxFailedAttempts()) {
                throw new SecurityHardeningException(
                        "Account is locked due to multiple failed login attempts. Try again in " + (retryAfterSeconds / 60) + " minutes.",
                        "AUTH_003",
                        Collections.singletonMap("retryAfter", retryAfterSeconds)
                );
            } else {
                throw new SecurityHardeningException(
                        "Please wait " + retryAfterSeconds + " seconds before trying again.",
                        "AUTH_006",
                        Collections.singletonMap("retryAfter", retryAfterSeconds)
                );
            }
        }

        // 3. Validate user account status checks
        if (user.getStatus() == UserStatus.DISABLED) {
            log.warn("Login failed: Account {} is disabled", request.getEmail());
            eventPublisher.publishEvent(new LoginFailureEvent(this, request.getEmail(), loginContext, "Account is disabled"));
            throw new SecurityHardeningException("Account has been disabled. Contact support.", "AUTH_002");
        }

        if (user.getStatus() == UserStatus.PENDING_VERIFICATION) {
            log.warn("Login failed: Email {} is not verified", request.getEmail());
            eventPublisher.publishEvent(new LoginFailureEvent(this, request.getEmail(), loginContext, "Email is not verified"));
            throw new SecurityHardeningException("Please verify your email before logging in.", "AUTH_002");
        }

        // 4. Validate password credentials
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLoginAttempt(user, loginContext, now);
            log.warn("Login failed: Password mismatch for {}", request.getEmail());
            throw new SecurityHardeningException("Invalid email or password", "AUTH_001");
        }

        // Login successful: reset failed login attempts, update login metadata
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setNextAllowedLoginAt(null);
        user.setLastLoginAt(now);
        user.setLastLoginIp(loginContext.getIpAddress());
        user.setLastLoginUserAgent(loginContext.getUserAgent());
        userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(principal);
        
        // Pass IP and User Agent to createRefreshToken for device bindings
        RefreshTokenEntity refreshToken = tokenService.createRefreshToken(user, loginContext);

        Set<String> roles = principal.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toSet());

        // Publish LoginSuccessEvent
        eventPublisher.publishEvent(new LoginSuccessEvent(this, user, loginContext));

        log.info("User logged in successfully: {}", request.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getPlainToken()) // Return plain token to client
                .expiresIn(jwtService.getExpirationMs())
                .user(userMapper.toDto(user))
                .roles(roles)
                .permissions(Collections.emptySet())
                .build();
    }

    private void handleFailedLoginAttempt(UserEntity user, LoginContext loginContext, LocalDateTime now) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        long delaySeconds = 0;
        if (attempts == 2) delaySeconds = 2;
        else if (attempts == 3) delaySeconds = 5;
        else if (attempts == 4) delaySeconds = 15;
        
        List<Long> durations = securityProperties.getLock().getDurations();
        long lockMinutes = 0;
        if (attempts == 5) lockMinutes = durations.size() > 0 ? durations.get(0) : 30;
        else if (attempts == 6) lockMinutes = durations.size() > 1 ? durations.get(1) : 120;
        else if (attempts == 7) lockMinutes = durations.size() > 2 ? durations.get(2) : 720;
        else if (attempts == 8) lockMinutes = durations.size() > 3 ? durations.get(3) : 1440;

        if (lockMinutes > 0) {
            user.setStatus(UserStatus.LOCKED);
            user.setNextAllowedLoginAt(now.plusMinutes(lockMinutes));
            user.setAccountLockedUntil(now.plusMinutes(lockMinutes));
            
            // Publish AccountLockedEvent
            eventPublisher.publishEvent(new AccountLockedEvent(this, user, loginContext, lockMinutes));
            log.warn("Account {} locked for {} minutes due to {} failed attempts", user.getEmail(), lockMinutes, attempts);
        } else if (attempts >= 9) {
            user.setStatus(UserStatus.LOCKED);
            // Indefinite lock (requires password reset to unlock)
            user.setNextAllowedLoginAt(now.plusYears(100));
            user.setAccountLockedUntil(now.plusYears(100));
            
            eventPublisher.publishEvent(new AccountLockedEvent(this, user, loginContext, 999999L));
            log.warn("Account {} locked indefinitely. Password reset required.", user.getEmail());
        } else {
            user.setNextAllowedLoginAt(now.plusSeconds(delaySeconds));
        }

        userRepository.save(user);
        
        // Publish login failure event
        eventPublisher.publishEvent(new LoginFailureEvent(this, user.getEmail(), loginContext, "Invalid credentials"));
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        log.info("Processing logout request for token");
        String hashedToken = HashUtil.sha256(refreshToken);

        // Revoke the refresh token so it can't be used to mint new access tokens
        tokenService.revokeToken(hashedToken);

        // Stamp lastLogoutAt on the user so the JWT filter immediately rejects any
        // access token issued before this moment — kills stolen tokens with no blacklist.
        refreshTokenRepository.findByToken(hashedToken).ifPresentOrElse(
            storedToken -> {
                UserEntity user = storedToken.getUser();
                user.setLastLogoutAt(timeProvider.now());
                userRepository.save(user);
                log.info("Stamped lastLogoutAt for user: {}", user.getEmail());
            },
            () -> log.warn("Logout: refresh token not found; lastLogoutAt not stamped")
        );
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        log.info("Verifying email with token");
        String hashedToken = HashUtil.sha256(token);
        VerificationTokenEntity verificationToken = verificationTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new SecurityHardeningException("Invalid or expired verification token.", "AUTH_004"));

        if (verificationToken.getExpiryDate().isBefore(timeProvider.now())) {
            verificationTokenRepository.delete(verificationToken);
            throw new SecurityHardeningException("Invalid or expired verification token.", "AUTH_004");
        }

        UserEntity user = verificationToken.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
        log.info("Email verification complete for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resendVerification(String email) {
        log.info("Request to resend verification email to: {}", email);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new SecurityHardeningException("Email not found", "AUTH_001"));

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new IllegalArgumentException("Email is already verified.");
        }

        // Delete old token
        verificationTokenRepository.deleteByUser(user);

        // Generate new token
        String plainToken = UUID.randomUUID().toString();
        String hashedToken = HashUtil.sha256(plainToken);
        
        int verificationHours = securityProperties.getTokens().getVerificationExpiryHours();
        VerificationTokenEntity verificationToken = VerificationTokenEntity.builder()
                .token(hashedToken)
                .expiryDate(timeProvider.now().plusHours(verificationHours))
                .user(user)
                .build();
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), plainToken);
        log.info("Resent verification email to: {}", email);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        log.info("Request for password reset link for email: {}", email);
        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            // Return silently to prevent user enumeration
            log.info("Password reset request complete (email not found, handled silently)");
            return;
        }

        UserEntity user = userOpt.get();

        // Delete any old reset tokens
        passwordResetTokenRepository.deleteByUser(user);

        // Generate reset token (15-minute expiry)
        String plainToken = UUID.randomUUID().toString();
        String hashedToken = HashUtil.sha256(plainToken);
        
        int resetExpiryMinutes = securityProperties.getTokens().getResetExpiryMinutes();
        PasswordResetTokenEntity resetToken = PasswordResetTokenEntity.builder()
                .token(hashedToken)
                .expiryDate(timeProvider.now().plusMinutes(resetExpiryMinutes))
                .user(user)
                .build();
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), plainToken);
        log.info("Password reset token generated and logged for: {}", email);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        log.info("Processing password reset request");
        String hashedToken = HashUtil.sha256(request.getToken());
        PasswordResetTokenEntity resetToken = passwordResetTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new SecurityHardeningException("Invalid or expired reset token.", "AUTH_004"));

        if (resetToken.getExpiryDate().isBefore(timeProvider.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new SecurityHardeningException("Invalid or expired reset token.", "AUTH_004");
        }

        UserEntity user = resetToken.getUser();
        
        // Validate password reuse history limits (last 5 passwords)
        List<PasswordHistoryEntity> historyList = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(user);
        for (PasswordHistoryEntity hist : historyList) {
            if (passwordEncoder.matches(request.getPassword(), hist.getPasswordHash())) {
                throw new SecurityHardeningException("Password has been used recently. Please choose a different password.", "AUTH_007");
            }
        }
        
        // Also verify against the current password
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new SecurityHardeningException("Password has been used recently. Please choose a different password.", "AUTH_007");
        }

        // Save current password to history before updating it
        PasswordHistoryEntity oldPasswordHistory = PasswordHistoryEntity.builder()
                .user(user)
                .passwordHash(user.getPassword())
                .algorithm("BCRYPT")
                .build();
        passwordHistoryRepository.save(oldPasswordHistory);

        // Prune password history down to config limit
        int historyLimit = securityProperties.getPassword().getHistorySize();
        if (historyList.size() >= historyLimit) {
            for (int i = historyLimit - 1; i < historyList.size(); i++) {
                passwordHistoryRepository.delete(historyList.get(i));
            }
        }

        // Update password, reset login locks, transition to ACTIVE
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setLastPasswordChangedAt(timeProvider.now());
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        user.setNextAllowedLoginAt(null);
        if (user.getStatus() == UserStatus.LOCKED) {
            user.setStatus(UserStatus.ACTIVE);
        }
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        // Revoke all sessions (single sign-out on password change)
        tokenService.revokeAllUserTokens(user);

        log.info("Password reset successfully for user: {}", user.getEmail());
    }
}
