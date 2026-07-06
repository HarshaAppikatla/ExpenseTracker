package com.expenseflow.dashboard.controller;

import com.expenseflow.config.SecurityProperties;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.dashboard.dto.DashboardResponse;
import com.expenseflow.dashboard.service.DashboardService;
import com.expenseflow.insight.service.InsightService;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.annotation.DirtiesContext
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private DashboardService dashboardService;
    @MockBean private InsightService insightService;
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
    void testGetDashboardSummary_ReturnsOk() throws Exception {
        DashboardResponse summary = new DashboardResponse(
                new BigDecimal("100.00"), new BigDecimal("50.00"), new BigDecimal("30.00"), new BigDecimal("120.00"), Collections.emptyList(), Collections.emptyList()
        );

        UserEntity userEntity = new UserEntity();
        userEntity.setId("user-uuid");
        userEntity.setEmail("user@example.com");
        userEntity.setRoles(Collections.emptySet());
        UserPrincipal principal = new UserPrincipal(userEntity);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        when(dashboardService.getDashboardSummary(any())).thenReturn(summary);

        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.netBalance").value(120.00));
    }
}
