package com.expenseflow.repository;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.entity.SecurityEventEntity;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class SecurityEventRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private SecurityEventRepository securityEventRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindSecurityEvent_Success() {
        UserEntity user = UserEntity.builder()
                .fullName("Audit User")
                .email("audit.user@example.com")
                .password("password")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .build();
        userRepository.save(user);

        SecurityEventEntity event = SecurityEventEntity.builder()
                .user(user)
                .eventType("LOGIN_SUCCESS")
                .severity(com.expenseflow.entity.SecurityEventSeverity.INFO)
                .ipAddress("192.168.1.1")
                .userAgent("Mozilla/5.0")
                .requestId("req-abc")
                .correlationId("corr-abc")
                .traceId("trace-abc")
                .build();

        SecurityEventEntity saved = securityEventRepository.save(event);

        Optional<SecurityEventEntity> found = securityEventRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEventType()).isEqualTo("LOGIN_SUCCESS");
        assertThat(found.get().getSeverity()).isEqualTo(com.expenseflow.entity.SecurityEventSeverity.INFO);
        assertThat(found.get().getUser().getEmail()).isEqualTo("audit.user@example.com");
    }
}
