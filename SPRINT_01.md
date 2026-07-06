# SPRINT_01.md

# Sprint 01 – Authentication & User Management

Version: 1.0

Estimated Time: 5–7 Days

Status: Not Started

---

# Sprint Goal

Build a complete authentication system for ExpenseFlow.

This sprint establishes secure user authentication, authorization, and account lifecycle management.

No expense management, groups, trips, or financial logic should be implemented.

The authentication module should be production-ready and reusable for future features.

---

# Prerequisites

Completed:

* Sprint 00A
* Sprint 00B
* Sprint 00C

Read before implementation:

* MASTER_CONTEXT.md
* ENGINEERING_GUIDELINES.md
* PROJECT_STRUCTURE.md
* UI_STYLE_GUIDE.md
* TECH_STACK.md
* CODING_STANDARDS.md

---

# Sprint Scope

Included

* User Registration
* Login
* Logout
* JWT Authentication
* Refresh Token
* Email Verification
* Forgot Password
* Reset Password
* User Profile
* Protected Routes
* Session Persistence
* Role-Based Authorization (USER role only)
* Form Validation
* Password Strength Validation

Not Included

Groups

Trips

Expenses

Settlement

Reports

Notifications

OCR

AI Features

Admin Dashboard

---

# Frontend Pages

Create

/

Landing Page (update CTA buttons)

---

/login

Login Form

Fields

* Email
* Password

Features

* Remember Me
* Show/Hide Password
* Forgot Password Link
* Register Link

---

/register

Registration Form

Fields

* Full Name
* Email
* Password
* Confirm Password

Validation

Password strength meter

Terms & Conditions checkbox

Email uniqueness handled by backend

---

/forgot-password

Email input

Send Reset Link

---

/reset-password

Token validation

New Password

Confirm Password

---

/verify-email

Display verification success/failure

---

/profile

Display

* Avatar
* Name
* Email
* Account Status
* Created Date

Editing profile is out of scope.

---

# Backend Requirements

Entities

User

Role

RefreshToken

---

User Fields

* id (UUID)
* fullName
* email
* password
* emailVerified
* accountEnabled
* createdAt
* updatedAt
* createdBy
* updatedBy
* deletedAt
* deletedBy
* isDeleted

---

Role Fields

* id
* name

Default Role

ROLE_USER

---

RefreshToken Fields

* id
* token
* expiryDate
* userId

---

# APIs

POST

/api/v1/auth/register

POST

/api/v1/auth/login

POST

/api/v1/auth/logout

POST

/api/v1/auth/refresh

POST

/api/v1/auth/forgot-password

POST

/api/v1/auth/reset-password

GET

/api/v1/auth/verify-email

GET

/api/v1/users/me

---

# Authentication Flow

Registration

↓

Verification Email

↓

Verify Email

↓

Login

↓

Receive Access Token

*

Refresh Token

↓

Protected APIs

↓

Refresh Token

↓

Logout

---

# Security Requirements

BCrypt Password Encoding

JWT Access Token

JWT Refresh Token

Spring Security

Method Security Enabled

Role-Based Authorization

Prepare for future ROLE_ADMIN

---

# Frontend Architecture

Use

React Hook Form

Zod

TanStack Query

Axios Client

ProtectedRoute Component

Auth Context

Token Storage Strategy

Access Token

Memory

Refresh Token

HttpOnly Cookie (prepare architecture)

If cookies are not implemented yet, document the future migration.

---

# Validation Rules

Frontend

Email format

Password length

Password strength

Confirm Password

Required fields

Backend

Bean Validation

Unique Email

Password Policy

Verified Email required for login

---

# Email

Implement email sending abstraction.

Use Spring Mail.

Development may log verification links if SMTP is unavailable.

Prepare templates for:

* Verify Email
* Reset Password

---

# UI Requirements

Login Card

Centered

Glassmorphism style

Register Card

Modern SaaS layout

Responsive

Mobile-friendly

Material Design 3

Use existing design system.

---

# Error Handling

Display user-friendly messages.

Examples

Invalid Credentials

Email Already Registered

Email Not Verified

Token Expired

Password Too Weak

Account Disabled

Do not expose internal errors.

---

# Database Changes

Create Flyway migration

V2__authentication.sql

Tables

users

roles

refresh_tokens

Insert default role

ROLE_USER

---

# Acceptance Criteria

User can register.

Verification email generated.

Email verification works.

Verified user can log in.

JWT generated.

Refresh token generated.

Protected routes work.

Session persists after refresh.

Logout invalidates refresh token.

Forgot password flow works.

Reset password works.

Profile endpoint returns authenticated user.

No console errors.

No lint errors.

Responsive UI verified.

---

# Manual Testing Checklist

Register new user.

Attempt duplicate registration.

Verify email.

Attempt login before verification.

Verify login succeeds after verification.

Refresh access token.

Logout.

Attempt API after logout.

Forgot password.

Reset password.

Reload browser.

Verify session persistence.

---

# Deliverables

Production-ready authentication module.

Secure backend authentication.

Responsive frontend authentication.

Reusable auth architecture.

Prepared for future role expansion.

---

# Out of Scope

Dashboard Widgets

Groups

Trips

Expenses

Settlement

Reports

Notifications

Admin Panel

Financial Logic

---

# Definition of Done

Sprint is complete only if:

All authentication flows work.

JWT validated.

Refresh token works.

Protected routes enforced.

Email verification implemented.

Forgot password implemented.

Reset password implemented.

Profile endpoint works.

No security warnings.

No compilation errors.

Ready for Sprint 02.

---

# Prompt for Antigravity

Read:

MASTER_CONTEXT.md

ENGINEERING_GUIDELINES.md

PROJECT_STRUCTURE.md

UI_STYLE_GUIDE.md

TECH_STACK.md

CODING_STANDARDS.md

Implement ONLY Sprint 01.

Do not implement dashboard features, groups, trips, expenses, or reports.

Follow the existing architecture exactly.

Use Flyway for database migrations.

Use DTOs for all APIs.

Never expose entities directly.

Use constructor injection only.

Use TanStack Query for API communication.

Follow Material Design 3 and the established design system.

Stop after the authentication module is complete and validated.
