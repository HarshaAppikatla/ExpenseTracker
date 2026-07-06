package com.expenseflow.profile.service;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.profile.dto.OnboardingRequest;
import com.expenseflow.profile.dto.UserProfileResponse;
import com.expenseflow.profile.entity.UserProfileEntity;
import com.expenseflow.profile.event.UserProfileOnboardedEvent;
import com.expenseflow.profile.mapper.UserProfileMapper;
import com.expenseflow.profile.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserProfileMapper userProfileMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserProfileService userProfileService;

    private UserEntity testUser;
    private OnboardingRequest request;
    private UserProfileEntity profileEntity;
    private UserProfileResponse profileResponse;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId("user-uuid");
        testUser.setEmail("user@example.com");

        request = new OnboardingRequest("USD", new BigDecimal("100.00"), new BigDecimal("5000.00"));

        profileEntity = UserProfileEntity.builder()
                .id("profile-uuid")
                .user(testUser)
                .preferredCurrency("USD")
                .openingBalance(new BigDecimal("100.00"))
                .onboardingCompleted(true)
                .build();

        profileResponse = new UserProfileResponse(
                "profile-uuid",
                "user-uuid",
                "USD",
                new BigDecimal("100.00"),
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void testGetProfile_Success() {
        when(userProfileRepository.findByUserId("user-uuid")).thenReturn(Optional.of(profileEntity));
        when(userProfileMapper.toResponse(profileEntity)).thenReturn(profileResponse);

        UserProfileResponse result = userProfileService.getProfile("user-uuid");

        assertThat(result).isNotNull();
        assertThat(result.preferredCurrency()).isEqualTo("USD");
        verify(userProfileRepository).findByUserId("user-uuid");
    }

    @Test
    void testGetProfile_NotFound_ThrowsException() {
        when(userProfileRepository.findByUserId("user-uuid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileService.getProfile("user-uuid"))
                .isInstanceOf(SecurityHardeningException.class)
                .hasMessageContaining("User profile not found")
                .extracting(e -> ((SecurityHardeningException) e).getCode())
                .isEqualTo("PROF_001");
    }

    @Test
    void testOnboardUser_Success() {
        when(userProfileRepository.findByUserId("user-uuid")).thenReturn(Optional.empty());
        when(userProfileRepository.save(any(UserProfileEntity.class))).thenReturn(profileEntity);
        when(userProfileMapper.toResponse(profileEntity)).thenReturn(profileResponse);

        UserProfileResponse result = userProfileService.onboardUser(testUser, request);

        assertThat(result).isNotNull();
        assertThat(result.onboardingCompleted()).isTrue();

        // Verify Event Publishing
        ArgumentCaptor<UserProfileOnboardedEvent> eventCaptor = ArgumentCaptor.forClass(UserProfileOnboardedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        UserProfileOnboardedEvent event = eventCaptor.getValue();
        assertThat(event.getUser()).isEqualTo(testUser);
        assertThat(event.getPreferredCurrency()).isEqualTo("USD");
        assertThat(event.getOpeningBalance()).isEqualByComparingTo("100.00");
        assertThat(event.getInitialMonthlyIncome()).isEqualByComparingTo("5000.00");
    }

    @Test
    void testOnboardUser_AlreadyOnboarded_ThrowsException() {
        when(userProfileRepository.findByUserId("user-uuid")).thenReturn(Optional.of(profileEntity));

        assertThatThrownBy(() -> userProfileService.onboardUser(testUser, request))
                .isInstanceOf(SecurityHardeningException.class)
                .hasMessageContaining("already onboarded");
    }
}
