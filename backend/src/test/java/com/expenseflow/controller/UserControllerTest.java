package com.expenseflow.controller;

import com.expenseflow.config.SecurityProperties;
import com.expenseflow.dto.user.UserDto;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.security.JwtAuthenticationFilter;
import com.expenseflow.security.JwtService;
import com.expenseflow.security.UserDetailsServiceImpl;
import com.expenseflow.security.UserPrincipal;
import com.expenseflow.service.UserService;
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

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@org.springframework.test.annotation.DirtiesContext
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private UserService userService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private SecurityProperties securityProperties;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testGetMe_AuthenticatedUser_ReturnsProfile() throws Exception {
        UserEntity user = new UserEntity();
        user.setEmail("user@example.com");
        user.setFullName("User User");
        user.setRoles(Collections.emptySet());
        
        UserPrincipal principal = new UserPrincipal(user);
        
        UserDto profileDto = UserDto.builder()
                .email("user@example.com")
                .fullName("User User")
                .build();

        when(userService.getCurrentUserProfile("user@example.com")).thenReturn(profileDto);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        mockMvc.perform(get("/api/v1/users/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("User User"));

        verify(userService).getCurrentUserProfile("user@example.com");
    }
}
