package com.expenseflow.controller;

import com.expenseflow.config.SecurityProperties;
import com.expenseflow.dto.auth.*;
import com.expenseflow.dto.user.UserDto;
import com.expenseflow.security.JwtAuthenticationFilter;
import com.expenseflow.security.JwtService;
import com.expenseflow.security.UserDetailsServiceImpl;
import com.expenseflow.service.AuthService;
import com.expenseflow.service.TokenService;
import com.expenseflow.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters to isolate endpoint contracts
@org.springframework.test.annotation.DirtiesContext
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private UserService userService;
    @MockBean private TokenService tokenService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private SecurityProperties securityProperties;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testRegister_ValidPayload_ReturnsOk() throws Exception {
        RegisterRequest request = new RegisterRequest("Test User", "test@example.com", "Password123!");
        UserDto responseDto = UserDto.builder()
                .email("test@example.com")
                .fullName("Test User")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful. Verification email has been sent."))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Test User"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void testRegister_InvalidEmail_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("Test User", "invalid-email", "Password123!");

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void testCheckEmail_EmailAvailable_ReturnsAvailable() throws Exception {
        CheckEmailRequest request = new CheckEmailRequest("new@example.com");
        when(userService.existsByEmail("new@example.com")).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/check-email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.available").value(true));

        verify(userService).existsByEmail("new@example.com");
    }

    @Test
    void testLogin_ValidCredentials_ReturnsTokens() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "Password123!");
        LoginResponse response = LoginResponse.builder()
                .accessToken("access-token-123")
                .refreshToken("refresh-token-123")
                .expiresIn(900000L)
                .roles(Collections.singleton("ROLE_USER"))
                .build();

        when(authService.login(any(LoginRequest.class), any(LoginContext.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token-123"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token-123"))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"));

        verify(authService).login(any(LoginRequest.class), any(LoginContext.class));
    }

    @Test
    void testRefresh_ValidToken_ReturnsNewTokens() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest("old-refresh-token");
        TokenRefreshResponse response = TokenRefreshResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .expiresIn(900000L)
                .build();

        when(tokenService.rotateRefreshToken(eq("old-refresh-token"), any(LoginContext.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"));

        verify(tokenService).rotateRefreshToken(eq("old-refresh-token"), any(LoginContext.class));
    }

    @Test
    void testLogout_ValidToken_RevokesToken() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest("refresh-token-to-revoke");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService).logout("refresh-token-to-revoke");
    }

    @Test
    void testForgotPassword_ValidEmail_SendsResetEmail() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService).forgotPassword("test@example.com");
    }

    @Test
    void testResetPassword_ValidPayload_ResetsPassword() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("reset-token-123", "NewPassword123!");

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService).resetPassword(any(ResetPasswordRequest.class));
    }
}
