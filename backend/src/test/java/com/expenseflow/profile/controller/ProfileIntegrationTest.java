package com.expenseflow.profile.controller;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import com.expenseflow.profile.dto.OnboardingRequest;
import com.expenseflow.profile.entity.UserProfileEntity;
import com.expenseflow.profile.repository.UserProfileRepository;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProfileIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private UserEntity testUser;
    private UserPrincipal testPrincipal;

    @BeforeEach
    void setUp() {
        userProfileRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        testUser = UserEntity.builder()
                .fullName("Onboard Integrator")
                .email("onboard.integration@example.com")
                .password("Password123!")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(testUser);

        testPrincipal = new UserPrincipal(testUser);
    }

    @Test
    void testUserProfileOnboardingAndLifecycleFlow() throws Exception {
        // 1. Fetch Profile when not onboarded -> Expect Error (PROF_001)
        mockMvc.perform(get("/api/v1/profile")
                        .with(user(testPrincipal)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PROF_001"));

        // 2. Perform Onboarding
        OnboardingRequest onboardReq = new OnboardingRequest("INR", new BigDecimal("5000.00"), null);

        mockMvc.perform(post("/api/v1/profile/onboarding")
                        .with(user(testPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(onboardReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.preferredCurrency").value("INR"))
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true));

        // Assert record exists in DB
        assertThat(userProfileRepository.findByUserId(testUser.getId())).isPresent();

        // 3. Fetch Profile again -> Expect Success
        mockMvc.perform(get("/api/v1/profile")
                        .with(user(testPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.preferredCurrency").value("INR"))
                .andExpect(jsonPath("$.data.openingBalance").value(5000.00));

        // 4. Update Profile Settings
        OnboardingRequest updateReq = new OnboardingRequest("USD", new BigDecimal("12000.00"), null);
        mockMvc.perform(put("/api/v1/profile")
                        .with(user(testPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.preferredCurrency").value("USD"))
                .andExpect(jsonPath("$.data.openingBalance").value(12000.00));
    }
}
