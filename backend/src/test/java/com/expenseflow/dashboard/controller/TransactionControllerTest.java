package com.expenseflow.dashboard.controller;

import com.expenseflow.config.SecurityProperties;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.dashboard.dto.TransactionDto;
import com.expenseflow.dashboard.service.TransactionService;
import com.expenseflow.repository.UserRepository;
import com.expenseflow.security.JwtAuthenticationFilter;
import com.expenseflow.security.JwtService;
import com.expenseflow.security.UserDetailsServiceImpl;
import com.expenseflow.security.UserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.annotation.DirtiesContext
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private TransactionService transactionService;
    @MockBean private UserRepository userRepository;
    
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
    void testSearchTransactions_ReturnsOk() throws Exception {
        UserEntity userEntity = new UserEntity();
        userEntity.setId("user-uuid");
        userEntity.setEmail("user@example.com");
        userEntity.setRoles(Collections.emptySet());
        UserPrincipal principal = new UserPrincipal(userEntity);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        when(transactionService.searchTransactions(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
