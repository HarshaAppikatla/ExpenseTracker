package com.expenseflow.service;

public interface EmailService {
    void sendVerificationEmail(String email, String token);
    void sendPasswordResetEmail(String email, String token);
}
