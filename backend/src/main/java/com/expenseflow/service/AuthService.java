package com.expenseflow.service;

import com.expenseflow.dto.auth.LoginRequest;
import com.expenseflow.dto.auth.LoginResponse;
import com.expenseflow.dto.auth.RegisterRequest;
import com.expenseflow.dto.auth.ResetPasswordRequest;
import com.expenseflow.dto.auth.LoginContext;
import com.expenseflow.dto.user.UserDto;

public interface AuthService {
    UserDto register(RegisterRequest request);
    LoginResponse login(LoginRequest request, LoginContext loginContext);
    void logout(String refreshToken);
    void verifyEmail(String token);
    void resendVerification(String email);
    void forgotPassword(String email);
    void resetPassword(ResetPasswordRequest request);
}
