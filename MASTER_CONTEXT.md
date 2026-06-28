# MASTER_CONTEXT.md

# ExpenseFlow

> Master Project Context
>
> This document contains the permanent context for the ExpenseFlow project.
>
> Every sprint assumes this document has been read.
>
> Do not contradict this document unless explicitly instructed.

---

# 1. Project Overview

ExpenseFlow is a modern expense management platform designed for both personal finance and collaborative expense sharing.

Unlike traditional expense trackers, ExpenseFlow treats every group trip as a collaborative workspace instead of a simple collection of expenses.

The application should be scalable, production-ready, and maintainable.

---

# 2. Product Vision

The goal is to build an application that allows users to:

* Track personal expenses
* Create groups
* Organize trips
* Record expenses
* Split bills
* Calculate settlements
* Monitor budgets
* Generate reports
* Archive completed trips

Future versions will include:

* AI receipt scanning
* OCR
* Smart budgeting
* Travel memories
* Offline synchronization

---

# 3. Core Philosophy

ExpenseFlow is NOT an accounting application.

ExpenseFlow is a collaborative workspace.

The user experience should feel similar to:

Google Calendar

*

Notion

*

Splitwise

*

Google Photos

The application should prioritize simplicity over complexity.

---

# 4. Primary Users

* Friends
* Families
* Roommates
* Office Teams
* College Students
* Event Organizers

---

# 5. Technology Stack

Frontend

* React
* TypeScript
* Vite
* Tailwind CSS
* Material UI

Backend

* Spring Boot 3
* Spring Security
* Spring Data JPA
* JWT Authentication

Database (Development)

MySQL

Database (Production)

PostgreSQL (Supabase)

Build Tool

Maven

Version Control

Git

Hosting

Frontend

Vercel

Backend

Render

Future

Docker

Kubernetes

---

# 6. Architecture

Use Layered Architecture.

Presentation Layer

↓

Controller

↓

Service

↓

Repository

↓

Database

Controllers must NEVER contain business logic.

Business logic belongs only inside Services.

---

# 7. Backend Standards

Always use

DTOs

Never expose Entities directly.

Use UUID instead of auto-increment IDs whenever practical.

Use Lombok.

Use Global Exception Handling.

Use Validation Annotations.

Use ResponseEntity.

Every response should follow

ApiResponse<T>

Example

{
success,

message,

data,

timestamp
}

---

# 8. Frontend Standards

Use

Functional Components

Hooks

React Router

Reusable Components

Feature-based Folder Structure

Avoid prop drilling where possible.

Create shared UI components.

Do not use inline CSS.

Prefer Tailwind utilities.

Material UI components may be customized using Tailwind.

---

# 9. Folder Structure

frontend/

components/

features/

hooks/

layouts/

pages/

routes/

services/

types/

utils/

backend/

controller/

service/

repository/

entity/

dto/

config/

security/

exception/

mapper/

util/

database/

schema/

seed/

migration/

docs/

---

# 10. UI Principles

Modern

Minimal

Professional

Responsive

Fast

Do NOT create cluttered interfaces.

Use whitespace generously.

Primary Design Style

Material Design 3

Dashboard Style

Bento Grid

Landing Page

Glassmorphism

Trip Workspace

Timeline Layout

Animations

Subtle only.

---

# 11. Coding Principles

Follow SOLID Principles.

Keep methods small.

Avoid duplicate logic.

Prefer composition over inheritance.

Always write readable code.

Do not prematurely optimize.

---

# 12. Git Strategy

Main Branch

main

Development Branch

development

Feature Branches

feature/<feature-name>

Bug Fix

bugfix/<issue>

Every sprint ends with

Git Commit

↓

Push

↓

Tag Release

---

# 13. Business Rules

Financial records must never be permanently deleted.

Use Soft Delete.

Every entity should include

createdAt

updatedAt

createdBy

updatedBy

deletedAt

deletedBy

Expenses automatically recalculate balances after:

Create

Update

Delete

Settlement

Historical records must always remain consistent.

---

# 14. Security

Passwords

BCrypt

Authentication

JWT

Authorization

Role Based

Prevent

SQL Injection

XSS

CSRF

Validate all user inputs.

---

# 15. Performance Goals

Dashboard

< 2 seconds

Expense Creation

< 1 second

Settlement Generation

< 3 seconds

Support

1000+

Expenses

100+

Trips

Without noticeable lag.

---

# 16. UI Components

Reusable Components

Button

Input

Modal

Drawer

Sidebar

Navbar

Card

Expense Card

Trip Card

Avatar

Chart

Dialog

Loading Skeleton

Empty State

Snackbar

Do not duplicate components.

---

# 17. Naming Conventions

Backend

UserController

UserService

UserRepository

UserEntity

UserDto

Frontend

ExpenseCard.tsx

TripCard.tsx

DashboardPage.tsx

LoginPage.tsx

GroupPage.tsx

Use PascalCase for Components.

camelCase for variables.

UPPER_SNAKE_CASE for constants.

---

# 18. Development Workflow

Every Sprint

Read MASTER_CONTEXT.md

↓

Read Current Sprint Document

↓

Implement ONLY current sprint.

↓

Do not implement future features.

↓

Run Tests

↓

Fix Bugs

↓

Commit

↓

Push

↓

Wait for next sprint.

---

# 19. AI Instructions

When generating code:

Do not modify completed modules unless explicitly requested.

Maintain existing folder structure.

Maintain coding standards.

Do not introduce unnecessary libraries.

Prefer clean architecture over quick implementation.

Always consider future scalability.

Whenever a feature requires backend and frontend changes, implement both.

Keep the code production-ready.

Do not generate placeholder implementations unless explicitly requested.

---

# 20. Definition of Done

A sprint is complete only if:

All planned features work.

Frontend and backend are integrated.

Database changes are complete.

Validation is implemented.

Responsive UI is working.

No compilation errors.

No console errors.

Code follows project conventions.

Git commit is ready.
