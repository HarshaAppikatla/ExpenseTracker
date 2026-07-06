package com.expenseflow.repository;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.entity.PasswordHistoryEntity;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class PasswordHistoryRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private PasswordHistoryRepository passwordHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndPasswordHistoryQuery_Success() {
        UserEntity user = UserEntity.builder()
                .fullName("History User")
                .email("history.user@example.com")
                .password("current-password")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .build();
        userRepository.save(user);

        PasswordHistoryEntity history1 = PasswordHistoryEntity.builder()
                .user(user)
                .passwordHash("old-hash-1")
                .algorithm("BCRYPT")
                .build();

        PasswordHistoryEntity history2 = PasswordHistoryEntity.builder()
                .user(user)
                .passwordHash("old-hash-2")
                .algorithm("BCRYPT")
                .build();

        passwordHistoryRepository.save(history1);
        passwordHistoryRepository.save(history2);

        List<PasswordHistoryEntity> historyList = passwordHistoryRepository.findByUserOrderByCreatedAtDesc(user);
        assertThat(historyList).hasSize(2);
        assertThat(historyList.get(0).getPasswordHash()).isEqualTo("old-hash-2"); // Ordered by createdAt desc
        assertThat(passwordHistoryRepository.countByUser(user)).isEqualTo(2L);
    }
}
