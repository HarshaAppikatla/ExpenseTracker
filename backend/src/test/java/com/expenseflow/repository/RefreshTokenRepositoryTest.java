package com.expenseflow.repository;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.entity.RefreshTokenEntity;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class RefreshTokenRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindByToken_Success() {
        UserEntity user = UserEntity.builder()
                .fullName("Token User")
                .email("token.user@example.com")
                .password("password")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .build();
        userRepository.save(user);

        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .token("hashed-refresh-token")
                .tokenFamily("family-uuid")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .user(user)
                .build();

        refreshTokenRepository.save(token);

        Optional<RefreshTokenEntity> found = refreshTokenRepository.findByToken("hashed-refresh-token");
        assertThat(found).isPresent();
        assertThat(found.get().getTokenFamily()).isEqualTo("family-uuid");
    }

    @Test
    void testDeleteByUser_RevokesTokens() {
        UserEntity user = UserEntity.builder()
                .fullName("Token User")
                .email("token.user2@example.com")
                .password("password")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .build();
        userRepository.save(user);

        RefreshTokenEntity token = RefreshTokenEntity.builder()
                .token("hashed-refresh-token-2")
                .tokenFamily("family-uuid")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .user(user)
                .build();

        refreshTokenRepository.save(token);

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        Optional<RefreshTokenEntity> found = refreshTokenRepository.findByToken("hashed-refresh-token-2");
        assertThat(found).isEmpty();
    }
}
