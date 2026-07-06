package com.expenseflow.income.controller;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import com.expenseflow.income.dto.IncomeRequest;
import com.expenseflow.income.entity.IncomeEntity;
import com.expenseflow.income.repository.IncomeRepository;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class IncomeIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    private UserEntity testUser;
    private UserPrincipal testPrincipal;

    @BeforeEach
    void setUp() {
        incomeRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        testUser = UserEntity.builder()
                .fullName("Income Integrator")
                .email("inc.integration@example.com")
                .password("Password123!")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .roles(Collections.emptySet())
                .build();
        userRepository.save(testUser);

        testPrincipal = new UserPrincipal(testUser);
    }

    @Test
    void testIncomeLifecycleFlow() throws Exception {
        // 1. Create an Income Transaction
        IncomeRequest request = new IncomeRequest(
                new BigDecimal("1500.00"), "USD", "Paycheck", LocalDateTime.now(), "Bi-weekly salary", null
        );

        mockMvc.perform(post("/api/v1/income")
                        .with(user(testPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.source").value("Paycheck"))
                .andExpect(jsonPath("$.data.amount").value(1500.00));

        // Resolve income ID
        Pageable pageable = PageRequest.of(0, 10);
        Page<IncomeEntity> incomePage = incomeRepository.findByUserIdAndIsDeletedFalse(testUser.getId(), pageable);
        assertThat(incomePage.getContent()).hasSize(1);
        String incomeId = incomePage.getContent().get(0).getId();

        // 2. Fetch Income List -> Expect 1 record
        mockMvc.perform(get("/api/v1/income")
                        .with(user(testPrincipal)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));

        // 3. Update the Income record
        IncomeRequest updateRequest = new IncomeRequest(
                new BigDecimal("1600.00"), "USD", "Paycheck", LocalDateTime.now(), "Bi-weekly salary + bonus", null
        );
        mockMvc.perform(put("/api/v1/income/" + incomeId)
                        .with(user(testPrincipal))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.amount").value(1600.00));

        // 4. Delete the Income record (Soft delete)
        mockMvc.perform(delete("/api/v1/income/" + incomeId)
                        .with(user(testPrincipal))
                        .with(csrf()))
                .andExpect(status().isOk());

        // Verify it is soft deleted and excluded from active pages
        Page<IncomeEntity> postDeletePage = incomeRepository.findByUserIdAndIsDeletedFalse(testUser.getId(), pageable);
        assertThat(postDeletePage.getContent()).isEmpty();
    }
}
