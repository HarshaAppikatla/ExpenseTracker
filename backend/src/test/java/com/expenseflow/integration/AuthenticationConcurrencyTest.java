package com.expenseflow.integration;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.dto.auth.LoginContext;
import com.expenseflow.dto.auth.TokenRefreshResponse;
import com.expenseflow.entity.RefreshTokenEntity;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.repository.RefreshTokenRepository;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuthenticationConcurrencyTest extends BaseIntegrationTest {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private UserEntity testUser;
    private LoginContext loginContext;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        testUser = UserEntity.builder()
                .fullName("Concurrent User")
                .email("concurrent@example.com")
                .password("Password123!")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .build();
        userRepository.save(testUser);

        loginContext = LoginContext.builder()
                .ipAddress("127.0.0.1")
                .userAgent("Mozilla/5.0")
                .build();
    }

    @Test
    void testConcurrentTokenRotation_OneSucceedsOtherIsReplay() throws InterruptedException, ExecutionException {
        // Arrange
        RefreshTokenEntity token = tokenService.createRefreshToken(testUser, loginContext);
        String plainToken = token.getPlainToken();

        int threads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(1);

        List<Callable<TokenRefreshResponse>> tasks = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            tasks.add(() -> {
                latch.await(); // wait for latch trigger
                return tokenService.rotateRefreshToken(plainToken, loginContext);
            });
        }

        latch.countDown(); // Start tasks simultaneously
        List<Future<TokenRefreshResponse>> futures = executor.invokeAll(tasks);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger replayExceptionCount = new AtomicInteger();

        for (Future<TokenRefreshResponse> future : futures) {
            try {
                TokenRefreshResponse response = future.get();
                if (response != null) {
                    successCount.incrementAndGet();
                }
            } catch (ExecutionException e) {
                if (e.getCause() instanceof SecurityHardeningException she) {
                    if ("AUTH_005".equals(she.getCode())) {
                        replayExceptionCount.incrementAndGet();
                    }
                }
            }
        }

        executor.shutdown();

        // Assert: One rotation must succeed, the other must trigger replay protection (AUTH_005)
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(replayExceptionCount.get()).isEqualTo(1);
    }
}
