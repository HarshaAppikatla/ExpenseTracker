# ADR 004: Layered Backend Architecture

## Status
Approved

## Context
A clear, clean backend separation of concerns is necessary to ensure the ExpenseFlow database, business logic, and presentation endpoints remain modular, testable, and isolated.

## Decision
We adopt a Classic Layered Architecture:
1. **Presentation Layer (Controllers):** Handles HTTP mapping, JSON validation, and formats output. Zero business logic.
2. **Service Layer (Services & Impls):** Contains all domain calculations, transaction definitions, and business validations.
3. **Data Access Layer (Repositories):** Interface mappings to the database via Spring Data JPA. Zero business logic.

## Consequences
- Controllers must never call Repositories directly; they must communicate through the Service interface layer.
- All request/response objects passed through Controllers must be mapped using DTOs via MapStruct; database Entity models are restricted to the Service/Repository boundary.
