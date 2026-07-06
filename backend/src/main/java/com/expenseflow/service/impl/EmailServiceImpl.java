package com.expenseflow.service.impl;

import com.expenseflow.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private static final String FRONTEND_URL = "http://localhost:5173";

    @Override
    public void sendVerificationEmail(String email, String token) {
        String verificationUrl = FRONTEND_URL + "/verify-email?token=" + token;
        
        log.info("----------------------------------------------------------------");
        log.info("EMAIL TRIGGERED [Verification Email]");
        log.info("To: {}", email);
        log.info("Subject: Verify Your ExpenseFlow Account");
        log.info("Verification URL: {}", verificationUrl);
        log.info("----------------------------------------------------------------");
    }

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        String resetUrl = FRONTEND_URL + "/reset-password?token=" + token;

        log.info("----------------------------------------------------------------");
        log.info("EMAIL TRIGGERED [Password Reset Email]");
        log.info("To: {}", email);
        log.info("Subject: Reset Your ExpenseFlow Password");
        log.info("Password Reset URL: {}", resetUrl);
        log.info("----------------------------------------------------------------");
    }
}
