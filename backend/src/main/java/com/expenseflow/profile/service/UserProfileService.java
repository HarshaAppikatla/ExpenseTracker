package com.expenseflow.profile.service;

import com.expenseflow.entity.UserEntity;
import com.expenseflow.exception.SecurityHardeningException;
import com.expenseflow.profile.dto.OnboardingRequest;
import com.expenseflow.profile.dto.UserProfileResponse;
import com.expenseflow.profile.entity.UserProfileEntity;
import com.expenseflow.profile.event.UserProfileOnboardedEvent;
import com.expenseflow.profile.mapper.UserProfileMapper;
import com.expenseflow.profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final com.expenseflow.repository.UserRepository userRepository;

    public UserProfileResponse onboardUser(String userId, OnboardingRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return onboardUser(user, request);
    }


    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(String userId) {
        UserProfileEntity profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new SecurityHardeningException("User profile not found. Please complete onboarding.", "PROF_001"));
        return userProfileMapper.toResponse(profile);
    }

    @Transactional(readOnly = true)
    public boolean isOnboardingCompleted(String userId) {
        return userProfileRepository.findByUserId(userId)
                .map(UserProfileEntity::isOnboardingCompleted)
                .orElse(false);
    }

    public UserProfileResponse onboardUser(UserEntity user, OnboardingRequest request) {
        if (userProfileRepository.findByUserId(user.getId()).isPresent()) {
            throw new SecurityHardeningException("User is already onboarded.", "PROF_002");
        }

        UserProfileEntity profile = UserProfileEntity.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .preferredCurrency(request.preferredCurrency())
                .openingBalance(request.openingBalance())
                .onboardingCompleted(true)
                .build();

        UserProfileEntity saved = userProfileRepository.save(profile);
        log.info("User {} successfully completed onboarding with currency {}", user.getEmail(), request.preferredCurrency());

        // Publish event to decouple default income seeding
        eventPublisher.publishEvent(new UserProfileOnboardedEvent(
                this, user, request.preferredCurrency(), request.openingBalance(), request.initialMonthlyIncome()));

        return userProfileMapper.toResponse(saved);
    }

    public UserProfileResponse updateProfile(String userId, OnboardingRequest request) {
        UserProfileEntity profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new SecurityHardeningException("User profile not found. Please complete onboarding.", "PROF_001"));

        profile.setPreferredCurrency(request.preferredCurrency());
        profile.setOpeningBalance(request.openingBalance());
        
        UserProfileEntity saved = userProfileRepository.save(profile);
        log.info("User profile updated successfully for userId: {}", userId);
        return userProfileMapper.toResponse(saved);
    }
}
