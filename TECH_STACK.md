# TECH_STACK.md

# ExpenseFlow Technology Stack

> This document defines the official technology stack, dependency versions, coding tools, and development environment for ExpenseFlow.
>
> Every sprint must use only the technologies listed here unless explicitly approved.

---

# 1. Project Overview

ExpenseFlow is a modern full-stack web application.

Architecture

Frontend

↓

REST APIs

↓

Backend

↓

Database

The application should remain modular and scalable.

---

# 2. Development Environment

Operating System

Windows 11

Recommended IDE

Visual Studio Code

Recommended Java IDE

IntelliJ IDEA Community Edition

Terminal

PowerShell

---

# 3. Java

Version

Java 21 LTS

Reason

Long-term support

Better performance

Modern language features

---

# 4. Spring Boot

Version

3.5.x (latest stable available when starting the project)

Dependencies

Spring Web

Spring Security

Spring Validation

Spring Data JPA

MySQL Driver

Spring Boot Actuator

Lombok

SpringDoc OpenAPI (Swagger)

DevTools

Future

Spring Mail

Spring Cache

WebSocket

---

# 5. Build Tool

Maven

Project Structure

Standard Maven Layout

---

# 6. Database

Development

MySQL 8.x

Production

PostgreSQL (Supabase)

Migration Tool

Flyway (recommended)

Character Set

UTF-8

Timezone

UTC

---

# 7. Frontend

Framework

React

Language

TypeScript

Build Tool

Vite

Package Manager

npm

Routing

React Router

HTTP Client

Axios

State Management

React Context (initially)

Future

Redux Toolkit or Zustand if application complexity requires it.

---

# 8. Styling

Primary

Tailwind CSS

Component Library

Material UI

Icons

Lucide React

Fonts

Inter

Animation

Framer Motion

Charts

Recharts

Notifications

React Hot Toast

---

# 9. Authentication

JWT

Access Token

Refresh Token

Password Encryption

BCrypt

Role Based Authorization

Future

OAuth (Google Login)

---

# 10. API Documentation

Swagger UI

OpenAPI 3

Every endpoint must be documented.

---

# 11. File Storage

Development

Local Storage

Production

Supabase Storage

Future

Cloudinary (optional)

---

# 12. Development Tools

ESLint

Prettier

Husky

lint-staged

EditorConfig

These tools should enforce code quality automatically.

---

# 13. Testing

Frontend

Vitest

React Testing Library

Backend

JUnit 5

Mockito

Spring Boot Test

Manual

Postman

---

# 14. Version Control

Git

Repository

GitHub (created after Sprint 0 validation)

Branch Strategy

main

development

feature/*

bugfix/*

release/*

---

# 15. Deployment

Frontend

Vercel

Backend

Render

Database

Supabase

Future

Docker

Docker Compose

Kubernetes

NGINX

GitHub Actions

---

# 16. Folder Structure

Frontend

feature-based architecture

Backend

layered architecture

Database

version-controlled migrations

Documentation

Markdown only

---

# 17. Code Quality Rules

Backend

* DTOs for all APIs
* Global Exception Handling
* Validation Annotations
* Layered Architecture
* Service interfaces with implementations where appropriate

Frontend

* Functional Components
* TypeScript Strict Mode
* Reusable Components
* No inline styles
* No duplicated logic

---

# 18. Naming Standards

React Components

PascalCase

TypeScript Files

camelCase where appropriate

Java Classes

PascalCase

Packages

lowercase

Database

snake_case

API

kebab-case URLs

---

# 19. Recommended Extensions

Visual Studio Code

* ESLint
* Prettier
* Tailwind CSS IntelliSense
* Material Icon Theme
* Error Lens
* GitLens
* Thunder Client

---

# 20. AI Development Rules

When generating code:

Use only the technologies defined in this document.

Do not introduce additional frameworks.

Do not replace selected libraries with alternatives.

Maintain compatibility across every sprint.

If a dependency upgrade is required, update this document before modifying the project.

Technology consistency is more important than using the newest library.

---

# 21. Version Lock

The following choices are considered architectural decisions.

These should not change during development without strong justification.

* React + TypeScript
* Vite
* Tailwind CSS
* Material UI
* Spring Boot
* Spring Security
* Spring Data JPA
* MySQL (Development)
* PostgreSQL / Supabase (Production)
* Maven
* Axios
* JWT Authentication
* React Context (initial)
* Recharts
* Framer Motion
* Lucide React

Changing any of these after Sprint 0 should be treated as an architecture change and documented before implementation.
