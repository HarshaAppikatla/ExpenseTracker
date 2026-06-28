# SPRINT_00C.md

# Sprint 00C – Frontend & Backend Integration

Version: 1.0

Estimated Time: 3–5 Hours

Status: Not Started

---

# Sprint Goal

Connect the React frontend with the Spring Boot backend and verify that the complete application foundation works correctly.

This sprint validates the project architecture before implementing any business features.

No authentication.

No users.

No groups.

No expenses.

No database tables.

Only infrastructure integration.

---

# Read Before Starting

Read the following documents.

* MASTER_CONTEXT.md
* ENGINEERING_GUIDELINES.md
* PROJECT_STRUCTURE.md
* UI_STYLE_GUIDE.md
* TECH_STACK.md

Also ensure Sprint 00A and Sprint 00B are fully completed.

---

# Sprint Scope

Included

* Axios Configuration
* API Service Layer
* Environment Variables
* Health API Integration
* Toast Notifications
* Global Loading Component
* Global Error Handling
* Backend Status Card
* Theme Verification

Not Included

Authentication

JWT

Users

Groups

Trips

Expenses

Settlement

Reports

Database Tables

---

# Frontend Tasks

Configure Axios

Create

services/

api/

axios.ts

Requirements

Base URL from environment variables.

Request timeout.

JSON headers.

Centralized Axios instance.

---

# Environment Variables

Frontend

.env

VITE_API_BASE_URL=http://localhost:8080/api/v1

Never hardcode URLs.

---

# API Layer

Create

HealthService

Method

checkHealth()

No other services.

Future services will follow this pattern.

---

# Backend Tasks

Verify

Health endpoint

GET /api/v1/health

No additional endpoints.

---

# Dashboard Placeholder

Create a temporary Dashboard.

Display

Backend Status

Frontend Status

Environment

Application Version

Current Theme

The page exists only to verify connectivity.

---

# Backend Status Card

Show

Backend

🟢 Online

or

🔴 Offline

Use Health API response.

---

# Loading State

Create reusable

LoadingSpinner

Display while Health API request is running.

---

# Error State

Create reusable

ErrorCard

Display

Unable to connect to backend.

Retry Button

Do not display technical error messages.

---

# Snackbar

Configure React Hot Toast.

Display

Backend Connected

Backend Offline

Only for testing.

---

# API Response Handling

Frontend must consume

ApiResponse<T>

No custom parsing.

All future APIs should use the same structure.

---

# Theme Verification

Verify

Material UI Theme

Tailwind

Dark Mode Configuration

Responsive Layout

No UI redesign.

---

# CORS Verification

Verify frontend

localhost:5173

can communicate with

localhost:8080

No CORS errors.

---

# Folder Structure

Create

services/

api/

HealthService.ts

types/

ApiResponse.ts

components/

common/

LoadingSpinner.tsx

ErrorCard.tsx

StatusCard.tsx

---

# Manual Testing

Start Backend.

Start Frontend.

Open Dashboard.

Verify

Backend Status

↓

Online

Stop Backend.

Refresh.

Verify

Backend Status

↓

Offline

Restart Backend.

Verify

Status returns to Online.

---

# Acceptance Criteria

Frontend communicates with backend.

Health endpoint works.

Axios configured.

Environment variables working.

Loading component visible.

Error component visible.

Toast notifications working.

Responsive layout verified.

No console errors.

No CORS errors.

---

# Deliverables

Connected React application.

Connected Spring Boot application.

Working Health API integration.

Reusable API layer.

Reusable Loading component.

Reusable Error component.

Backend Status Dashboard.

Ready for Authentication.

---

# Out of Scope

Login

Register

JWT

Database Tables

Entities

Repositories

Groups

Trips

Expenses

Reports

Notifications

Settlement

---

# Definition of Done

Sprint is complete only if:

Frontend and backend communicate successfully.

Health API integrated.

Axios configured.

Loading states implemented.

Error states implemented.

No CORS issues.

Responsive layout verified.

Application architecture ready for feature development.

---

# Foundation Review Checklist

Before proceeding to Sprint 01:

✓ Frontend builds successfully.

✓ Backend builds successfully.

✓ Health API returns HTTP 200.

✓ Swagger works.

✓ Material UI configured.

✓ Tailwind configured.

✓ Folder structure follows PROJECT_STRUCTURE.md.

✓ No unnecessary dependencies.

✓ No console warnings.

✓ No lint warnings.

✓ No compilation errors.

Only after all checklist items pass should the project proceed to Sprint 01.

---

# Prompt for Antigravity

Read:

MASTER_CONTEXT.md

ENGINEERING_GUIDELINES.md

PROJECT_STRUCTURE.md

UI_STYLE_GUIDE.md

TECH_STACK.md

Implement ONLY Sprint 00C.

Do not implement authentication.

Do not create entities.

Do not create users.

Do not create JWT.

Do not create business APIs.

Focus only on integrating frontend and backend cleanly using the Health API.

The project should be fully validated and ready for feature development.
