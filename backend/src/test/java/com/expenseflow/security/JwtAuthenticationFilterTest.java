package com.expenseflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserDetailsServiceImpl userDetailsService;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;
    @Mock private UserPrincipal userDetails;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testFilter_ValidJwt_AuthenticatesUser() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-jwt-token");
        when(jwtService.validateToken("valid-jwt-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("valid-jwt-token")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        
        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.isAccountNonLocked()).thenReturn(true);
        
        Date now = new Date();
        when(jwtService.getIssuedAt("valid-jwt-token")).thenReturn(now);
        when(userDetails.isIssuedBeforeLogout(now)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testFilter_JwtIssuedBeforeLogout_RejectsToken() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer old-jwt-token");
        when(jwtService.validateToken("old-jwt-token")).thenReturn(true);
        when(jwtService.getEmailFromToken("old-jwt-token")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        
        when(userDetails.isEnabled()).thenReturn(true);
        when(userDetails.isAccountNonLocked()).thenReturn(true);
        
        Date pastDate = new Date(System.currentTimeMillis() - 5000);
        when(jwtService.getIssuedAt("old-jwt-token")).thenReturn(pastDate);
        when(userDetails.isIssuedBeforeLogout(pastDate)).thenReturn(true); // Stale token!

        // Act
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testFilter_MissingJwt_Anonymous() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void testFilter_InvalidJwt_Anonymous() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-jwt-token");
        when(jwtService.validateToken("invalid-jwt-token")).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
