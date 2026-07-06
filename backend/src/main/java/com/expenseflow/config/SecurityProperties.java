package com.expenseflow.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "app.security")
@Validated
@Getter
@Setter
public class SecurityProperties {

    @Valid
    private Jwt jwt = new Jwt();

    @Valid
    private Password password = new Password();

    @Valid
    private Login login = new Login();

    @Valid
    private Session session = new Session();

    @Valid
    private Tokens tokens = new Tokens();

    @Valid
    private Lock lock = new Lock();

    @Getter
    @Setter
    public static class Jwt {
        @NotBlank(message = "JWT signing secret key must be configured")
        private String secret;

        @Min(value = 1, message = "Access token expiry must be at least 1 minute")
        @Max(value = 60, message = "Access token expiry must not exceed 60 minutes")
        private int accessTokenExpiryMinutes = 15;

        @Min(value = 1, message = "Refresh token expiry must be at least 1 day")
        @Max(value = 30, message = "Refresh token expiry must not exceed 30 days")
        private int refreshTokenExpiryDays = 7;
    }

    @Getter
    @Setter
    public static class Password {
        @Min(value = 8, message = "Password minimum length must be at least 8 characters")
        private int minLength = 8;

        @Max(value = 128, message = "Password maximum length must not exceed 128 characters")
        private int maxLength = 128;

        @Min(value = 1, message = "Password history size must track at least 1 entry")
        @Max(value = 20, message = "Password history size limit is 20 entries")
        private int historySize = 5;
    }

    @Getter
    @Setter
    public static class Login {
        @Min(value = 1, message = "Max failed login attempts must be at least 1")
        @Max(value = 20, message = "Max failed login attempts must not exceed 20")
        private int maxFailedAttempts = 5;

        @Min(value = 1, message = "Decay interval must be at least 1 minute")
        private int decayIntervalMinutes = 30;
    }

    @Getter
    @Setter
    public static class Session {
        @Min(value = 1, message = "Idle timeout must be at least 1 minute")
        private int idleTimeoutMinutes = 30;

        @Min(value = 1, message = "Absolute timeout must be at least 1 hour")
        private int absoluteTimeoutHours = 8;
    }

    @Getter
    @Setter
    public static class Tokens {
        @Min(value = 1, message = "Email verification token expiry must be at least 1 hour")
        private int verificationExpiryHours = 24;

        @Min(value = 1, message = "Password reset token expiry must be at least 1 minute")
        private int resetExpiryMinutes = 15;
    }

    @Getter
    @Setter
    public static class Lock {
        @NotEmpty(message = "Progressive lockout durations list must not be empty")
        private List<Long> durations = List.of(30L, 120L, 720L, 1440L);
    }
}
