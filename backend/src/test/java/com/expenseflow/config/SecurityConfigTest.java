package com.expenseflow.config;

import com.expenseflow.controller.UserController;
import com.expenseflow.security.JwtAuthenticationFilter;
import com.expenseflow.security.JwtService;
import com.expenseflow.security.UserDetailsServiceImpl;
import com.expenseflow.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = true)
@org.springframework.test.annotation.DirtiesContext
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private UserService userService;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private SecurityProperties securityProperties;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() throws Exception {
        // Stub the mocked JwtAuthenticationFilter to proceed with the filter chain
        doAnswer(invocation -> {
            jakarta.servlet.ServletRequest req = invocation.getArgument(0);
            jakarta.servlet.ServletResponse res = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(req, res);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    void testPublicEndpoint_Health_IsAccessible() throws Exception {
        // Even though WebMvcTest is targeting UserController, we can call security checks
        // because SecurityConfig applies globally to mockMvc request matching.
        mockMvc.perform(get("/api/v1/health")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // NotFound indicates request reached routing (bypassed security) since UserController doesn't map it.
    }

    @Test
    void testProtectedEndpoint_WithoutToken_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().exists("Content-Security-Policy"));
    }
}
