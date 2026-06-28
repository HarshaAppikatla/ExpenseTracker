# SPRINT_00B.md

# Sprint 00B – Backend Foundation

Version: 1.0

Estimated Time: 4–6 Hours

Status: Not Started

---

# Sprint Goal

Build the backend foundation for ExpenseFlow.

This sprint focuses ONLY on backend architecture and configuration.

No authentication.

No business logic.

No entities.

No database tables.

The backend should become production-ready before implementing any features.

---

# Read Before Starting

Read these documents carefully.

* MASTER_CONTEXT.md
* ENGINEERING_GUIDELINES.md
* PROJECT_STRUCTURE.md
* UI_STYLE_GUIDE.md
* TECH_STACK.md

---

# Sprint Scope

Included

* Spring Boot Project
* Maven Configuration
* Package Structure
* Spring Security Base Configuration
* Swagger Configuration
* MySQL Connection
* Global Exception Handler
* Common API Response Wrapper
* Health Check API
* Validation Configuration
* Environment Configuration

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

Repositories

Business Services

---

# Spring Boot Setup

Create Spring Boot project.

Java Version

21

Dependencies

Spring Web

Spring Security

Spring Validation

Spring Data JPA

MySQL Driver

Lombok

SpringDoc OpenAPI

DevTools

---

# Package Structure

Create exactly this structure.

com.expenseflow

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

---

# Configuration

Configure

application.yml

application-dev.yml

application-prod.yml

Configure

Server Port

Database Connection

Logging

Swagger

CORS

Timezone

UTF-8

---

# Spring Security

Create a basic Spring Security configuration.

Requirements

Allow all requests temporarily.

Disable authentication for now.

Prepare configuration for future JWT integration.

Do not implement login.

Do not create filters.

Do not create JWT classes.

---

# Global Exception Handling

Create

GlobalExceptionHandler

Handle

Validation Errors

Internal Server Errors

Illegal Arguments

Generic Exceptions

Return consistent API responses.

---

# API Response Format

Every endpoint must return

{
"success": true,
"message": "",
"data": {},
"timestamp": ""
}

Create a reusable ApiResponse class.

---

# Health Check API

Endpoint

GET /api/v1/health

Response

{
"success": true,
"message": "ExpenseFlow Backend Running",
"data": {
"status": "UP",
"version": "1.0.0"
}
}

---

# Swagger

Configure Swagger.

Requirements

Swagger UI opens.

Health endpoint visible.

Future APIs automatically documented.

---

# Database

Create MySQL database

expenseflow_dev

Verify

Connection established.

No tables should be created.

No entities.

No repositories.

---

# Logging

Configure

Console Logging

Request Logging

Error Logging

Do not log sensitive information.

---

# Validation

Configure

Bean Validation

Global Validation Error Handling

No feature validations yet.

---

# CORS

Allow frontend

http://localhost:5173

Future deployments should be configurable.

---

# Deliverables

Working Spring Boot application.

Working MySQL connection.

Swagger UI.

Health API.

ApiResponse wrapper.

Global Exception Handler.

Production-ready backend structure.

---

# Acceptance Criteria

Spring Boot starts successfully.

No startup errors.

Swagger UI accessible.

Health endpoint returns success.

MySQL connection verified.

Package structure complete.

No warnings.

No authentication implemented.

---

# Manual Testing

Backend starts.

Swagger opens.

Health API returns HTTP 200.

No exceptions.

Database connection successful.

---

# Out of Scope

Login

Register

JWT

Users

Roles

Groups

Trips

Expenses

Settlement

Reports

Email

Notifications

---

# Definition of Done

Sprint is complete only if:

Backend runs successfully.

Health API works.

Swagger UI works.

MySQL connection verified.

ApiResponse implemented.

Global exception handler implemented.

No compilation warnings.

Project ready for Sprint 00C.

---

# Prompt for Antigravity

Read:

MASTER_CONTEXT.md

ENGINEERING_GUIDELINES.md

PROJECT_STRUCTURE.md

UI_STYLE_GUIDE.md

TECH_STACK.md

Implement ONLY this sprint.

Do not implement authentication.

Do not generate business entities.

Do not create database tables.

Focus only on backend architecture and configuration.

The backend must be production-ready and ready for integration with the frontend in Sprint 00C.
