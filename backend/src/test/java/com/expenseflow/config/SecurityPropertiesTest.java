package com.expenseflow.config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityPropertiesTest {

    @Test
    void testSecurityPropertiesDefaults() {
        SecurityProperties properties = new SecurityProperties();

        // JWT Defaults
        assertThat(properties.getJwt().getAccessTokenExpiryMinutes()).isEqualTo(15);
        assertThat(properties.getJwt().getRefreshTokenExpiryDays()).isEqualTo(7);

        // Password Defaults
        assertThat(properties.getPassword().getMinLength()).isEqualTo(8);
        assertThat(properties.getPassword().getMaxLength()).isEqualTo(128);
        assertThat(properties.getPassword().getHistorySize()).isEqualTo(5);

        // Login Defaults
        assertThat(properties.getLogin().getMaxFailedAttempts()).isEqualTo(5);
        assertThat(properties.getLogin().getDecayIntervalMinutes()).isEqualTo(30);

        // Session Defaults
        assertThat(properties.getSession().getIdleTimeoutMinutes()).isEqualTo(30);
        assertThat(properties.getSession().getAbsoluteTimeoutHours()).isEqualTo(8);

        // Tokens Defaults
        assertThat(properties.getTokens().getVerificationExpiryHours()).isEqualTo(24);
        assertThat(properties.getTokens().getResetExpiryMinutes()).isEqualTo(15);

        // Lock Defaults
        assertThat(properties.getLock().getDurations()).containsExactly(30L, 120L, 720L, 1440L);
    }

    @Test
    void testSecurityPropertiesSettersAndGetters() {
        SecurityProperties properties = new SecurityProperties();
        
        properties.getJwt().setSecret("custom-secret-key-1234567890-custom-secret-key");
        properties.getJwt().setAccessTokenExpiryMinutes(30);
        properties.getJwt().setRefreshTokenExpiryDays(14);
        
        properties.getPassword().setMinLength(10);
        properties.getPassword().setMaxLength(64);
        properties.getPassword().setHistorySize(10);

        properties.getLogin().setMaxFailedAttempts(3);
        properties.getLogin().setDecayIntervalMinutes(15);

        properties.getSession().setIdleTimeoutMinutes(20);
        properties.getSession().setAbsoluteTimeoutHours(4);

        properties.getTokens().setVerificationExpiryHours(12);
        properties.getTokens().setResetExpiryMinutes(10);

        properties.getLock().setDurations(List.of(15L, 45L));

        assertThat(properties.getJwt().getSecret()).isEqualTo("custom-secret-key-1234567890-custom-secret-key");
        assertThat(properties.getJwt().getAccessTokenExpiryMinutes()).isEqualTo(30);
        assertThat(properties.getJwt().getRefreshTokenExpiryDays()).isEqualTo(14);
        assertThat(properties.getPassword().getMinLength()).isEqualTo(10);
        assertThat(properties.getPassword().getMaxLength()).isEqualTo(64);
        assertThat(properties.getPassword().getHistorySize()).isEqualTo(10);
        assertThat(properties.getLogin().getMaxFailedAttempts()).isEqualTo(3);
        assertThat(properties.getLogin().getDecayIntervalMinutes()).isEqualTo(15);
        assertThat(properties.getSession().getIdleTimeoutMinutes()).isEqualTo(20);
        assertThat(properties.getSession().getAbsoluteTimeoutHours()).isEqualTo(4);
        assertThat(properties.getTokens().getVerificationExpiryHours()).isEqualTo(12);
        assertThat(properties.getTokens().getResetExpiryMinutes()).isEqualTo(10);
        assertThat(properties.getLock().getDurations()).containsExactly(15L, 45L);
    }
}
