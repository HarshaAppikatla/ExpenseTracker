package com.expenseflow.income.service;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.income.dto.IncomeRequest;
import com.expenseflow.income.dto.IncomeResponse;
import com.expenseflow.income.entity.IncomeEntity;
import com.expenseflow.income.mapper.IncomeMapper;
import com.expenseflow.income.repository.IncomeRepository;
import com.expenseflow.profile.event.UserProfileOnboardedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncomeServiceTest {

    @Mock private IncomeRepository incomeRepository;
    @Mock private IncomeMapper incomeMapper;
    @Mock private com.expenseflow.repository.UserRepository userRepository;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks private IncomeService incomeService;

    private UserEntity testUser;
    private IncomeRequest request;
    private IncomeEntity incomeEntity;
    private IncomeResponse incomeResponse;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId("user-uuid");
        testUser.setEmail("user@example.com");

        request = new IncomeRequest(
                new BigDecimal("350.00"), "USD", "Freelance", LocalDateTime.now(), "Project A payment", null
        );

        incomeEntity = IncomeEntity.builder()
                .id("inc-uuid")
                .user(testUser)
                .amount(new BigDecimal("350.00"))
                .currencyCode("USD")
                .source("Freelance")
                .incomeDate(LocalDateTime.now())
                .build();

        incomeResponse = new IncomeResponse(
                "inc-uuid", "user-uuid", new BigDecimal("350.00"), "USD", "Freelance", LocalDateTime.now(), "Project A payment", null, LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    void testCreateIncome_Success() {
        when(userRepository.findById("user-uuid")).thenReturn(Optional.of(testUser));
        when(incomeRepository.save(any(IncomeEntity.class))).thenReturn(incomeEntity);
        when(incomeMapper.toResponse(incomeEntity)).thenReturn(incomeResponse);

        IncomeResponse result = incomeService.createIncome(testUser.getId(), request);

        assertThat(result).isNotNull();
        assertThat(result.source()).isEqualTo("Freelance");
        verify(incomeRepository).save(any(IncomeEntity.class));
    }

    @Test
    void testGetIncomeById_AccessDenied_ThrowsException() {
        IncomeEntity otherUserIncome = IncomeEntity.builder()
                .id("inc-uuid")
                .user(UserEntity.builder().id("other-user-uuid").build())
                .build();

        when(incomeRepository.findById("inc-uuid")).thenReturn(Optional.of(otherUserIncome));

        assertThatThrownBy(() -> incomeService.getIncomeById("user-uuid", "inc-uuid"))
                .isInstanceOf(SecurityHardeningException.class)
                .hasMessageContaining("Access denied")
                .extracting(e -> ((SecurityHardeningException) e).getCode())
                .isEqualTo("INC_002");
    }

    @Test
    void testOnboardedEventListener_SeedsIncome() {
        UserProfileOnboardedEvent event = new UserProfileOnboardedEvent(
                this, testUser, "USD", new BigDecimal("1000.00"), new BigDecimal("4500.00")
        );

        incomeService.handleUserProfileOnboardedEvent(event);

        verify(incomeRepository).save(any(IncomeEntity.class));
    }
}
