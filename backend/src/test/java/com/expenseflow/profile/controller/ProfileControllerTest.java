package com.expenseflow.profile.controller;

import com.expenseflow.config.SecurityProperties;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.profile.dto.OnboardingRequest;
import com.expenseflow.profile.dto.UserProfileResponse;
import com.expenseflow.profile.service.UserProfileService;
import com.expenseflow.repository.UserRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.annotation.DirtiesContext
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private UserProfileService userProfileService;
    @MockBean private UserRepository userRepository;
    
    // Core Spring Security mocks required for ApplicationContext initialization
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private SecurityProperties securityProperties;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetProfile_ReturnsOk() throws Exception {
        UserProfileResponse responseDto = new UserProfileResponse(
                "profile-uuid", "user-uuid", "USD", new BigDecimal("100.00"), true, LocalDateTime.now(), LocalDateTime.now()
        );

        UserEntity userEntity = new UserEntity();
        userEntity.setId("user-uuid");
        userEntity.setEmail("user@example.com");
        userEntity.setRoles(Collections.emptySet());
        UserPrincipal principal = new UserPrincipal(userEntity);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        when(userProfileService.getProfile(any())).thenReturn(responseDto);

        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.preferredCurrency").value("USD"))
                .andExpect(jsonPath("$.data.openingBalance").value(100.00));
    }

    @Test
    void testOnboarding_InvalidPayload_ReturnsBadRequest() throws Exception {
        // Missing currency, negative balance
        OnboardingRequest invalidReq = new OnboardingRequest("", new BigDecimal("-10.00"), null);

        UserEntity userEntity = new UserEntity();
        userEntity.setId("user-uuid");
        userEntity.setEmail("user@example.com");
        userEntity.setRoles(Collections.emptySet());
        UserPrincipal principal = new UserPrincipal(userEntity);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        mockMvc.perform(post("/api/v1/profile/onboarding")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
