# ExpenseFlow Testing Strategy

This document establishes the testing standards, coverage metrics, and validation procedures for both backend and frontend applications.

---

## 1. Testing Philosophy
We employ a **Defense-in-Depth** validation strategy. Just as security is implemented in multiple layers, testing must occur at multiple granularities to prevent regressions:

```text
Unit Tests (Fast, Mocked)
       ↓
Integration Tests (Database, Security Filters)
       ↓
E2E Tests (Browser User Interface Flows)
```

---

## 2. Coverage Targets
To ensure production quality, all builds must satisfy:
- **Overall Line Coverage:** Minimum **80%**
- **Overall Branch Coverage:** Minimum **70%**
- **Service Layer (Business Logic):** Minimum **90%**
- **Controller/API Layer:** Minimum **80%**
- **Repository Layer:** Minimum **85%**
- **Security Layer:** Minimum **95%**
- **Critical Authentication Paths:** **100%**
- **PIT Mutation Score:** Minimum **75%**

---

## 3. Backend Testing Rules

### Unit Testing (JUnit 5 + Mockito)
- **Scope:** Service classes (`AuthServiceImpl`, `TokenServiceImpl`, `UserServiceImpl`).
- **Rule:** Mock all external dependencies (Repositories, EmailService, etc.).
- **Rule:** Utilize `TimeProvider` mock implementations to verify token expiration and lockout durations deterministically.

### Integration Testing (Spring Boot Test + Testcontainers MySQL 8.x)
- **Scope:** Security filter chains, JPA repositories, Flyway migrations, rate-limiting filters, concurrency conditions, and recovery logic.
- **Rule:** Verify that unauthorized paths (e.g. `/api/v1/users/**`) reject requests with HTTP 401 and the correct `AUTH_XXX` error code.
- **Rule:** Validate that Flyway migrations compile and database schemas are created successfully.
- **Rule:** Never use H2 database for JPA/integration testing. Testcontainers (MySQL 8.x) is the sole database engine.

---

## 4. Frontend Testing Rules

### Component & Hook Testing (Vitest + React Testing Library)
- **Scope:** Reusable forms (`LoginForm`, `RegisterForm`), hooks (`useAuth`), and guards (`ProtectedRoute`).
- **Rule:** Mock Axios client responses to simulate network errors and success states.
- **Rule:** Test that `PasswordStrength` accurately updates its visual indicator based on the password string value.

### E2E Testing (Playwright)
Every release must pass the primary E2E registration and session lifecycle scenario:
1. **User Sign Up:** Submit register form $\rightarrow$ verify success toast.
2. **Account Lock Attempt:** Fail login 5 times $\rightarrow$ verify `AUTH_003` (Account Locked) error and wait timer.
3. **Email Verification:** Load verify-email page with token $\rightarrow$ verify "Verified" checkmark.
4. **Successful Login:** Log in $\rightarrow$ verify redirect to Dashboard.
5. **Silent Session Restore:** Reload browser $\rightarrow$ verify no login screen flicker (AuthInitializer).
6. **Token Expiry Rotation:** Invalidate access token $\rightarrow$ verify silent refresh resolves.
7. **Session Idle Expiry:** Inactivity for 30 minutes $\rightarrow$ verify redirect to login with "Session expired" message.
8. **Logout:** Click logout $\rightarrow$ verify cookies/tokens deleted and redirected.
9. **Multi-Tab Logout:** Logging out in one tab triggers immediate logout in all other tabs.

---

## 5. Security Validation Checklist
Ensure every release satisfies the following checks:
- **JWT Tampering:** Modifying claims or signatures in the JWT returns `AUTH_005` (Invalid Signature).
- **Token Replay:** Reusing a refresh token triggers rotation revocation and signs out the token family.
- **Rate Limiting:** Sending requests above limits returns HTTP 429 and `RATE_001`.
- **Password History:** Resetting password to one of the last 5 hashes returns `AUTH_007`.
- **Information Leakage:** `/auth/check-email` only returns a boolean `available` and does not disclose profile status.
- **Security Headers:** Frame-Options: DENY, Content-Type-Options: nosniff, and Content-Security-Policy must be present in every response.
- **CORS Config:** Verify that the backend rejects origins outside the authorized developer domain (`http://localhost:5173`).
- **CSRF Behavior:** Document that CSRF is disabled only because our session management is stateless and token-based.
- **Audit Logging:** Check that all security events are persisted in `security_events` table and passwords/secrets are never printed in console.

