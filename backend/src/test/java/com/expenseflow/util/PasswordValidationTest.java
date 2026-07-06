package com.expenseflow.util;

import com.expenseflow.dto.auth.RegisterRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Password123!",
        "SecureP@ss1",
        "aB3#defg",
        "VeryLongPasswordThatIsSecure123#ButStillValid"
    })
    void testPasswordValidation_ValidPasswords_Passes(String password) {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .password(password)
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "short1!",       // Too short (< 8)
        "nopassword123", // No uppercase or special char
        "NOPASSWORD123", // No lowercase or special char
        "NoPassword!",   // No number
        "NoSpecial1",    // No special character
        "Password123!Password123!Password123!Password123!Password123!Password123!Password123!Password123!Password123!Password123!Password123!Password123!Password123!Password123!Password123!" // Too long (> 128)
    })
    void testPasswordValidation_InvalidPasswords_Fails(String password) {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("John Doe")
                .email("john.doe@example.com")
                .password(password)
                .build();

        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        assertThat(violations).isNotEmpty();
        ConstraintViolation<RegisterRequest> violation = violations.iterator().next();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("password");
        assertThat(violation.getMessage()).contains("Password must be 8-128 characters");
    }
}
