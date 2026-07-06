# SECURITY_HARDENING.md

# ExpenseFlow Security Hardening Guide

Version: 1.0

Status: Mandatory

This document defines the minimum security standards that every ExpenseFlow module must follow.

Authentication is only the first security layer.

Every future sprint must comply with this document.

---

# Security Philosophy

ExpenseFlow is a financial application.

Although it is not a banking application, it manages sensitive financial data.

Therefore the security model should follow industry best practices.

Never trust:

* User Input
* Client-side Validation
* Browser Storage
* JWT Claims without Verification
* External Requests

Always validate on the backend.

---

# Defense in Depth

Security must never rely on a single mechanism.

The system must implement multiple independent protection layers.

Internet

↓

Rate Limiting

↓

Bot Detection

↓

Authentication

↓

Authorization

↓

Business Validation

↓

Audit Logging

↓

Database

If one layer fails, another must still protect the application.

---

# Layer 1 — IP Rate Limiting

Purpose

Prevent automated attacks from a single IP.

Implementation

Use Bucket4j.

Limits

Login

5 requests / minute / IP

Register

3 requests / minute / IP

Forgot Password

3 requests / hour / IP

Refresh Token

30 requests / minute / IP

Verification

10 requests / hour / IP

Response

HTTP 429

Standard ApiResponse

Message

"Too many requests. Please try again later."

---

# Layer 2 — Email Rate Limiting

Track authentication attempts per email.

Purpose

Prevent attackers from targeting one account using multiple IPs.

Limits

Failed Login Attempts

20 per hour

Forgot Password

3 per hour

Verification Email

5 per day

Never expose whether the email exists.

Always return generic responses.

---

# Layer 3 — Progressive Login Delay

Every failed login increases the delay before the next attempt.

1st failure

0 seconds

2nd

2 seconds

3rd

5 seconds

4th

15 seconds

5th

60 seconds

10th

10 minutes

20th

1 hour

The delay should increase exponentially.

Never allow unlimited retries.

---

# Layer 4 — Adaptive Account Lock

Do NOT permanently lock accounts.

Use progressive lock durations.

Lock 1

30 minutes

Lock 2

2 hours

Lock 3

12 hours

Lock 4

24 hours

Lock 5+

Manual password reset required.

---

# Layer 5 — Failed Login Counter

Failed login attempts should NOT reset automatically after every lock.

Instead

After 30 minutes

Reduce failures gradually.

Example

5 failures

↓

30 minutes

↓

3 failures remain

↓

Another failure

↓

4 failures

↓

Eventually

↓

5

↓

Next lock

This prevents attackers from waiting out each lock period indefinitely.

---

# Layer 6 — Password Reset Recovery

Users may unlock accounts by resetting their password.

Process

Account Locked

↓

Password Reset

↓

Email Verification

↓

New Password

↓

Reset failedLoginAttempts

↓

Remove accountLockedUntil

↓

Status = ACTIVE

Password reset should invalidate all active refresh tokens.

---

# Layer 7 — Refresh Token Rotation

Every refresh request must:

Validate current refresh token.

↓

Issue new refresh token.

↓

Revoke previous refresh token.

↓

Store only the new token hash.

Old refresh tokens must never remain valid.

---

# Layer 8 — Token Hashing

Never store tokens in plain text.

Hash before storing.

Hash

Refresh Tokens

Verification Tokens

Password Reset Tokens

Algorithm

SHA-256

Database leaks should never expose reusable tokens.

---

# Layer 9 — JWT Security

Access Token

15 minutes

Refresh Token

7 days

Maximum Session

8 hours

Idle Timeout

30 minutes

Reject

Expired Tokens

Tampered Tokens

Modified Claims

Invalid Signatures

Wrong Algorithms

Never trust JWT payload without validation.

---

# Layer 10 — Idle Session Timeout

Track user activity.

Reset idle timer when:

Mouse movement

Keyboard input

Scroll

Touch

API requests

If inactive for

30 minutes

Automatically

Logout

↓

Clear tokens

↓

Redirect Login

↓

Display

"Session expired due to inactivity."

---

# Layer 11 — Absolute Session Timeout

Regardless of activity

Maximum authenticated session

8 hours

After 8 hours

Require login again.

Refresh tokens must not bypass this rule.

---

# Layer 12 — Multiple Browser Tabs

All browser tabs must stay synchronized.

Use

BroadcastChannel API

Fallback

localStorage events

Logout in one tab

↓

Logout everywhere

Session expiration

↓

All tabs redirect

---

# Layer 13 — Security Headers

Every response should include

X-Content-Type-Options

nosniff

X-Frame-Options

DENY

Referrer-Policy

strict-origin-when-cross-origin

Permissions-Policy

Content-Security-Policy

Strict-Transport-Security

(Production)

---

# Layer 14 — Audit Logging

Persist security events.

Log

Registration

Email Verification

Login Success

Login Failure

Logout

Password Reset

Password Change

Refresh Token

Account Lock

Account Unlock

Never log

Passwords

JWT

Refresh Tokens

Verification Tokens

Sensitive personal information

---

# Layer 15 — Login Notifications

Notify users when

Account Locked

Password Changed

Password Reset

New Device Login

Suspicious Login

Notification includes

Time

Browser

Operating System

Approximate Location

---

# Layer 16 — Device Sessions

Future Support

Users should be able to view active sessions.

Example

Windows Chrome

Android

iPhone

MacBook

Users can

Logout Current Device

Logout Other Devices

Logout All Devices

---

# Layer 17 — CAPTCHA

Do not require CAPTCHA for every login.

Instead enable CAPTCHA when

Repeated failures

High risk score

Bot detection

Suspicious IP

Cloudflare Turnstile is preferred.

---

# Layer 18 — Risk-Based Authentication

Calculate a security score.

Factors

New Device

New Country

VPN

Failed Attempts

Known Malicious IP

Time of Day

Risk Score

Low

↓

Allow Login

Medium

↓

Require CAPTCHA

High

↓

Require Email Verification

Critical

↓

Block Login

Notify User

---

# Layer 19 — Secure Coding Rules

Never concatenate SQL.

Never expose Entity objects.

Always use DTOs.

Always validate input.

Always sanitize output.

Always use constructor injection.

Never log secrets.

Never trust client-side validation.

---

# Layer 20 — Security Testing Checklist

Every release must verify

✓ SQL Injection

✓ XSS

✓ CSRF Protection

✓ JWT Validation

✓ Refresh Token Replay Protection

✓ Token Hashing

✓ Password Policy

✓ Account Lock

✓ Progressive Delay

✓ Adaptive Lock

✓ Password Reset

✓ Rate Limiting

✓ Security Headers

✓ Multi-Tab Logout

✓ Idle Timeout

✓ Absolute Session Timeout

✓ Audit Logging

✓ API Authorization

✓ Role Validation

✓ Sensitive Data Not Logged

No release should be considered production-ready unless every item passes.

---

# Future Security Enhancements

The following features are planned for later releases:

* Multi-Factor Authentication (MFA)
* Passkeys / WebAuthn
* Device Fingerprinting
* Geolocation-based Risk Detection
* Redis-backed JWT Blacklist
* Login History Dashboard
* Security Activity Timeline
* Password History Enforcement
* Admin Security Dashboard
* SIEM Integration
* Security Analytics

These are intentionally deferred and should not be implemented until the core product is stable.
