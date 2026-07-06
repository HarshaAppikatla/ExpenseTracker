package com.expenseflow.dashboard.controller;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import com.expenseflow.profile.entity.UserProfileEntity;
import com.expenseflow.profile.repository.UserProfileRepository;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
                .fullName("Dashboard Integrator")
                .email("dash.integration@example.com")
                .password("Password123!")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(testUser);

        testPrincipal = new UserPrincipal(testUser);

        UserProfileEntity profile = UserProfileEntity.builder()
                .id("prof-uuid-1")
                .user(testUser)
                .openingBalance(new BigDecimal("1000.00"))
                .preferredCurrency("USD")
                .build();
        userProfileRepository.save(profile);
    }

    @Test
    void testDashboardDynamicCalculationsFlow() throws Exception {
        // Query dynamic dashboard summary
        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .with(user(testPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.openingBalance").value(1000.00))
                .andExpect(jsonPath("$.data.netBalance").value(1000.00));
    }
}
