# ExpenseFlow Codebase & Architecture Evolution

This document tracks the high-level design shifts, domain-driven boundaries, and architectural patterns established across the development lifecycles of **ExpenseFlow**.

---

## 🗺️ Context Evolution Roadmap

```mermaid
graph TD
    subgraph Core Features (Sprints 01-05)
        User[User/Profile Context]
        Expense[Expense/Budget Context]
        Trip[Trip/Group Context]
    end

    subgraph Collaborative Calculus (Sprint 06)
        Settlement[Settlement Context]
        NetCalc[Net Balance Calculator]
        GreedyMatch[Greedy Debt Simplifier]
    end

    subgraph Real-Time Broadcasts (Sprint 07)
        Notif[Notification Context]
        SSE[SSE Stream Engine]
        CookieAuth[Jwt-Cookie Authentication Fallback]
    end

    Expense -->|Domain Events| Settlement
    Settlement -->|Domain Events| Notif
    Expense -->|Domain Events| Notif
    Notif -->|Broadcasting| SSE
```

---

## 🏗️ Architectural Foundations

### 1. Sprints 01–05: The Bounded Context Core
* **DDD Aggregate Boundaries**: Decoupled core layers (User, Expense, Trip) isolating services, converters, and controller endpoints.
* **Multi-Currency Policy (ADR-004)**: Embedded monetary value objects (`Money` type) encapsulating currency validations and fractional scales.

### 2. Sprint 06: Collaborative Debts & Minimization (ADR-005)
* **CQRS-lite Split**: Separate query read models from command mutation paths.
* **Domain Math Engine**:
  * `NetBalanceCalculator` translates group posted expenses into single-user net balances.
  * `DebtMinimizationSolver` implements a greedy matching network to simplify group debts from $O(N^2)$ complex edges down to $O(N)$ minimal transactions.
* **Auditability**: Cascade-owned history trail tables recording settlement dispute/resolution life cycles.

### 3. Sprint 07: Real-Time Event-Driven Architecture (ADR-006)
* **Decoupled Propagation**:
  * Core contexts publish transaction events (`BudgetLimitExceededEvent`, `SavingsGoalCompletedEvent`, etc.).
  * `NotificationEventListener` captures them asynchronously (`AFTER_COMMIT`) and persists alerts inside the Notification aggregate, maintaining strict transactional integrity.
* **Asynchronous SSE Stream Engine**:
  * `SseEmitterRegistry` caches active client connections safely across threads.
  * `SsePublisher` broadcasts payloads asynchronously (`@Async`) to the user registry.
  * `SseHeartbeatScheduler` emits 30-second heartbeats to maintain socket health.
* **Security Resilience**:
  * `JwtAuthenticationFilter` intercepts fallback cookies to support native browser `EventSource` handshakes.
  * Connection storms are mitigated via a 10-emitter FIFO eviction queue per user.
