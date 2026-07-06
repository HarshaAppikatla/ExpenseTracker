package com.expenseflow.income.controller;

import com.expenseflow.config.SecurityProperties;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.income.dto.IncomeRequest;
import com.expenseflow.income.dto.IncomeResponse;
import com.expenseflow.income.service.IncomeService;
import com.expenseflow.security.JwtAuthenticationFilter;
import com.expenseflow.security.JwtService;
import com.expenseflow.security.UserDetailsServiceImpl;
import com.expenseflow.security.UserPrincipal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncomeController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.annotation.DirtiesContext
class IncomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private IncomeService incomeService;
    
    // Core Spring security mocks
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private SecurityProperties securityProperties;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreateIncome_ValidPayload_ReturnsOk() throws Exception {
        IncomeRequest req = new IncomeRequest(
                new BigDecimal("5000.00"), "USD", "Salary", LocalDateTime.now(), "Monthly paycheck", null
        );
        IncomeResponse resp = new IncomeResponse(
                "inc-uuid", "user-uuid", new BigDecimal("5000.00"), "USD", "Salary", LocalDateTime.now(), "Monthly paycheck", null, LocalDateTime.now(), LocalDateTime.now()
        );

        UserEntity userEntity = new UserEntity();
        userEntity.setId("user-uuid");
        userEntity.setEmail("user@example.com");
        userEntity.setRoles(Collections.emptySet());
        UserPrincipal principal = new UserPrincipal(userEntity);

        when(incomeService.createIncome(any(String.class), any(IncomeRequest.class))).thenReturn(resp);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        mockMvc.perform(post("/api/v1/income")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").value(5000.00));
    }

    @Test
    void testCreateIncome_InvalidAmount_ReturnsBadRequest() throws Exception {
        IncomeRequest req = new IncomeRequest(
                new BigDecimal("-5.00"), "USD", "Salary", LocalDateTime.now(), "Monthly paycheck", null
        );

        UserEntity userEntity = new UserEntity();
        userEntity.setId("user-uuid");
        userEntity.setEmail("user@example.com");
        userEntity.setRoles(Collections.emptySet());
        UserPrincipal principal = new UserPrincipal(userEntity);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        mockMvc.perform(post("/api/v1/income")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
