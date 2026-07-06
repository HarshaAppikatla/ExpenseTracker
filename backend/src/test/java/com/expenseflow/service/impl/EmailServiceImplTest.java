package com.expenseflow.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class EmailServiceImplTest {

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailServiceImpl();
    }

    @Test
    void testSendVerificationEmail_Success() {
        assertThatCode(() -> emailService.sendVerificationEmail("user@example.com", "verify-token-123"))
                .doesNotThrowAnyException();
    }

    @Test
    void testSendPasswordResetEmail_Success() {
        assertThatCode(() -> emailService.sendPasswordResetEmail("user@example.com", "reset-token-123"))
                .doesNotThrowAnyException();
    }
}
