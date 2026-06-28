# SPRINT_00A.md

# Sprint 00A – Project Bootstrap & Local Foundation

Version: 1.0

Estimated Time: 1 Day

Status: Not Started

---

# Sprint Goal

Build the complete local development foundation for ExpenseFlow.

This sprint focuses ONLY on project setup.

No business features should be implemented.

No authentication.

No dashboard.

No database entities.

No expense logic.

The outcome of this sprint is a clean, production-ready project structure that is ready for future feature development.

---

# Prerequisites

Before starting, read the following files:

* MASTER_CONTEXT.md
* ENGINEERING_GUIDELINES.md
* PROJECT_STRUCTURE.md
* UI_STYLE_GUIDE.md
* TECH_STACK.md

These documents are the source of truth.

---

# Sprint Scope

Included

* Create frontend project
* Create backend project
* Configure Tailwind CSS
* Configure Material UI
* Configure React Router
* Configure Axios
* Configure Spring Boot
* Configure Spring Security (basic configuration only)
* Configure Swagger
* Configure MySQL connection
* Configure global exception handling
* Create folder structures
* Configure environment variables
* Create reusable layouts
* Create placeholder pages
* Create health check endpoint

Not Included

* Login
* Register
* JWT
* Users
* Groups
* Trips
* Expenses
* Settlement
* Reports
* Database tables
* Authentication logic

---

# Frontend Tasks

Create React project using

* Vite
* TypeScript

Install

* React Router
* Material UI
* Tailwind CSS
* Axios
* Framer Motion
* Lucide React
* React Hot Toast
* Recharts

Create folder structure exactly as defined in PROJECT_STRUCTURE.md

Create

src/

assets/

components/

contexts/

features/

hooks/

layouts/

pages/

routes/

services/

types/

utils/

constants/

styles/

Configure

* Tailwind
* Material UI Theme
* Global CSS
* Responsive Layout

Create placeholder pages

Landing

Dashboard

Profile

Settings

404

Each page should display a simple placeholder message only.

---

# Backend Tasks

Create Spring Boot project.

Install dependencies

* Spring Web
* Spring Security
* Spring Data JPA
* Validation
* Lombok
* MySQL Driver
* SpringDoc OpenAPI
* DevTools

Create package structure

config

controller

dto

entity

exception

mapper

repository

security

service

service.impl

util

validation

Create

HealthController

Endpoint

GET /api/v1/health

Response

{
"success": true,
"message": "ExpenseFlow Backend Running"
}

No business APIs should exist.

---

# Database Tasks

Install MySQL.

Create database

expenseflow_dev

Configure

application.yml

Connection

Timezone

Character Encoding

Do NOT create tables.

---

# UI Tasks

Landing Page

Modern hero section

Navigation bar

Footer

Glassmorphism style

Buttons should not perform any actions.

Dashboard

Placeholder cards only.

Sidebar

Top navigation

Responsive layout

No charts.

No API calls.

---

# Routing

Configure

/

Landing Page

/dashboard

Placeholder

/profile

Placeholder

/settings

Placeholder

404

Unknown Routes

---

# Environment Variables

Frontend

.env

VITE_API_BASE_URL=http://localhost:8080/api/v1

Backend

application.yml

Database URL

Username

Password

Server Port

Swagger

Never hardcode values.

---

# Code Quality

Configure

ESLint

Prettier

EditorConfig

Ensure

No lint warnings.

No formatting issues.

---

# Swagger

Enable

Swagger UI

Verify

OpenAPI documentation loads successfully.

No business endpoints required.

---

# Error Handling

Configure

Global Exception Handler

Default API response format

Do not implement custom exceptions yet.

---

# Acceptance Criteria

Frontend starts successfully.

Backend starts successfully.

Health endpoint works.

Swagger opens.

React Router works.

Material UI Theme applied.

Tailwind works.

Folder structure matches documentation.

No compilation errors.

No runtime errors.

No warnings.

Responsive layout verified.

---

# Manual Testing Checklist

Frontend

Application loads.

Landing page visible.

Sidebar renders.

Navigation works.

Responsive layout verified.

Backend

Application starts.

Health endpoint returns success.

Swagger UI opens.

Database connection successful.

No exceptions in logs.

---

# Expected Folder Structure

ExpenseFlow/

frontend/

backend/

database/

docs/

Both frontend and backend must strictly follow PROJECT_STRUCTURE.md.

---

# Deliverables

Working React application.

Working Spring Boot application.

Working MySQL connection.

Health Check API.

Swagger UI.

Configured project structure.

No business logic.

No authentication.

No entities.

No database tables.

Foundation only.

---

# Definition of Done

The sprint is complete only if:

* Frontend runs without errors.
* Backend runs without errors.
* Health API is accessible.
* Swagger UI is available.
* Tailwind and Material UI are configured.
* Folder structure is complete.
* All placeholder pages render correctly.
* Project is ready for Sprint 00B.

---

# Prompt for Antigravity

Read the following documents before generating code:

1. MASTER_CONTEXT.md
2. ENGINEERING_GUIDELINES.md
3. PROJECT_STRUCTURE.md
4. UI_STYLE_GUIDE.md
5. TECH_STACK.md

Implement ONLY the tasks defined in this sprint.

Do not create authentication.

Do not create database entities.

Do not implement business logic.

Do not generate unnecessary files.

Do not skip folder structure creation.

The result should be a clean, production-ready foundation that future sprints can build upon.
