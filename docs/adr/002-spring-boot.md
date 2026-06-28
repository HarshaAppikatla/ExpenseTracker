# ADR 002: Spring Boot 3.5.x Backend Framework

## Status
Approved

## Context
For the backend services of ExpenseFlow, we require a robust, enterprise-grade framework that supports security, transaction management, REST APIs, and database migrations.

## Decision
We adopt Spring Boot 3.5.x as the backend core runtime, using Java 21 LTS features.

## Consequences
- Leverage Jakarta EE validation, Spring Data JPA, and Spring Security natively.
- Ensures compatibility with Java 21 LTS virtual threads and performance optimizations.
- Relies on SpringDoc OpenAPI for auto-generated Swagger documentation.
- Integrates Spring Boot Actuator for health checks (readiness and liveness probes).
