package com.expenseflow.profile.repository;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import com.expenseflow.profile.entity.UserProfileEntity;
import com.expenseflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserProfileRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindProfile_Success() {
        UserEntity user = UserEntity.builder()
                .fullName("Profile Test User")
                .email("profile.test@example.com")
                .password("password")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .build();
        userRepository.save(user);

        UserProfileEntity profile = UserProfileEntity.builder()
                .id("profile-uuid-1")
                .user(user)
                .preferredCurrency("USD")
                .openingBalance(new BigDecimal("1500.00"))
                .onboardingCompleted(true)
                .build();

        userProfileRepository.save(profile);

        Optional<UserProfileEntity> foundByUserId = userProfileRepository.findByUserId(user.getId());
        assertThat(foundByUserId).isPresent();
        assertThat(foundByUserId.get().getPreferredCurrency()).isEqualTo("USD");
        assertThat(foundByUserId.get().getOpeningBalance()).isEqualByComparingTo("1500.00");

        Optional<UserProfileEntity> foundByEmail = userProfileRepository.findByUserEmail("profile.test@example.com");
        assertThat(foundByEmail).isPresent();
        assertThat(foundByEmail.get().getId()).isEqualTo("profile-uuid-1");
    }
}
