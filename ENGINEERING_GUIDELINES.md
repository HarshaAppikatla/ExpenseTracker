# ENGINEERING_GUIDELINES.md

# ExpenseFlow Engineering Guidelines

> This document defines how every feature in ExpenseFlow must be designed, implemented, tested, and maintained.
>
> Every sprint must follow these engineering rules.

---

# 1. General Principles

The project should always prioritize:

* Readability
* Scalability
* Maintainability
* Reusability
* Performance
* Security

The codebase should look like it was written by one engineer following one standard.

---

# 2. Development Philosophy

Always build production-ready code.

Do not create quick fixes.

Do not write temporary implementations.

Do not generate unnecessary code.

Every implementation should be extensible.

Whenever there are multiple valid solutions, choose the one that is easiest to maintain.

---

# 3. Project Development Order

The project must be developed in the following order.

Sprint 0

Project Setup

↓

Sprint 1

Authentication

↓

Sprint 2

Dashboard

↓

Sprint 3

Personal Expenses

↓

Sprint 4

Groups

↓

Sprint 5

Trips

↓

Sprint 6

Expense Engine

↓

Sprint 7

Split Engine

↓

Sprint 8

Settlement Engine

↓

Sprint 9

Timeline

↓

Sprint 10

Reports

↓

Sprint 11

Notifications

↓

Sprint 12

Deployment

Future features must never be implemented before their assigned sprint.

---

# 4. Backend Standards

Controllers

* Receive requests.
* Validate input.
* Call Services.
* Return ResponseEntity.
* Never contain business logic.

Services

* Contain all business rules.
* Handle transactions.
* Coordinate repositories.

Repositories

* Perform database operations only.
* Never contain business logic.

Entities

* Represent database tables only.
* No business logic.

DTOs

* Used for all API communication.
* Never expose Entity directly.

Mappers

* Convert Entity ↔ DTO.
* Prefer MapStruct where appropriate.

---

# 5. Frontend Standards

Pages

Represent complete screens.

Components

Reusable UI building blocks.

Hooks

Reusable business logic.

Services

API communication only.

Layouts

Shared application layouts.

Utils

Helper functions.

Types

Shared TypeScript interfaces.

Never mix UI rendering with API logic.

---

# 6. Feature Development Rules

Every new feature must include:

* Database changes (if required)
* Backend implementation
* API endpoints
* Frontend UI
* Validation
* Error handling
* Loading states
* Empty states
* Responsive design

A feature is incomplete if any of these are missing.

---

# 7. UI Guidelines

Use Material Design 3 principles.

Use Tailwind CSS utilities.

Avoid excessive animations.

Use consistent spacing.

Use an 8px spacing system.

Border Radius

12px–16px

Cards

Rounded

Soft Shadow

Buttons

Rounded

Primary CTA

Dashboard

Bento Grid

Trip

Timeline Layout

Landing Page

Glassmorphism

Consistency is more important than visual effects.

---

# 8. State Management

Use React Context for global application state where appropriate.

Use local component state for UI-only interactions.

Avoid unnecessary global state.

Introduce Redux Toolkit or Zustand only if application complexity requires it in later sprints.

---

# 9. API Design Standards

RESTful APIs only.

Naming

GET /api/v1/groups

POST /api/v1/groups

GET /api/v1/groups/{id}

PUT /api/v1/groups/{id}

DELETE /api/v1/groups/{id}

Always use plural resource names.

Version every API.

Return consistent response formats.

---

# 10. Error Handling

Backend

Use Global Exception Handler.

Frontend

Display meaningful error messages.

Never expose stack traces.

Never expose SQL errors.

Never expose internal implementation details.

---

# 11. Validation Rules

Frontend validation

*

Backend validation

Both are required.

Never trust client-side validation alone.

---

# 12. Logging

Log:

Authentication events

Expense creation

Settlement generation

Critical failures

Do not log passwords.

Do not log JWT tokens.

Do not log sensitive financial information.

---

# 13. Security Standards

Passwords

BCrypt

JWT Authentication

Refresh Tokens

Role-Based Authorization

Input Validation

Output Encoding

Parameterized Queries

CORS Configuration

Rate Limiting

Future:

Two-Factor Authentication

---

# 14. Database Standards

Every table must include:

id

created_at

updated_at

created_by

updated_by

deleted_at

deleted_by

is_deleted

Use UUIDs for identifiers.

Prefer foreign key constraints.

Normalize the schema appropriately.

---

# 15. Testing Requirements

Each sprint must include:

Backend Tests

* Service Layer
* Repository Layer (where applicable)

Frontend Tests

* Component rendering
* Form validation

Manual Testing

* End-to-end verification of sprint features

---

# 16. Git Standards

Commit Message Format

feat: add authentication module

fix: resolve settlement calculation bug

refactor: simplify expense service

docs: update sprint documentation

test: add unit tests for login service

One feature per commit where practical.

---

# 17. Performance Guidelines

Avoid unnecessary database queries.

Avoid duplicate API calls.

Implement pagination for large datasets.

Lazy load heavy components.

Optimize bundle size.

---

# 18. AI Coding Rules

When implementing code:

Read MASTER_CONTEXT.md first.

Read current sprint document.

Do not implement future features.

Do not rename existing folders.

Do not introduce additional frameworks.

Keep architecture consistent.

If requirements are ambiguous, choose the solution that best matches existing project conventions.

---

# 19. Code Review Checklist

Before considering a sprint complete:

* Code compiles successfully.
* No warnings.
* No console errors.
* No unused code.
* No duplicate logic.
* Responsive UI verified.
* APIs tested.
* Database migration verified.
* Documentation updated.

---

# 20. Definition of Engineering Excellence

ExpenseFlow should be maintainable by another engineer without additional explanation.

Every module should be:

* Modular
* Testable
* Documented
* Reusable
* Consistent

The project should resemble a production SaaS application rather than a prototype or academic submission.
