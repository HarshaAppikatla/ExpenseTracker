package com.expenseflow.repository;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindByEmail_Success() {
        UserEntity user = UserEntity.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .password("hashed-password")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .build();

        userRepository.save(user);

        Optional<UserEntity> found = userRepository.findByEmail("john.doe@example.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
        assertThat(found.get().isDeleted()).isFalse();
    }

    @Test
    void testUniqueEmailConstraint_ThrowsException() {
        UserEntity user1 = UserEntity.builder()
                .fullName("User One")
                .email("dup@example.com")
                .password("pass")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .build();

        UserEntity user2 = UserEntity.builder()
                .fullName("User Two")
                .email("dup@example.com")
                .password("pass")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .build();

        userRepository.save(user1);

        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void testSoftDelete_ExcludesFromQueries() {
        UserEntity user = UserEntity.builder()
                .fullName("Jane Doe")
                .email("jane.doe@example.com")
                .password("hashed-password")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .build();

        UserEntity savedUser = userRepository.saveAndFlush(user);

        // Soft delete
        userRepository.delete(savedUser);
        userRepository.flush();

        Optional<UserEntity> found = userRepository.findByEmail("jane.doe@example.com");
        assertThat(found).isEmpty();
    }
}
