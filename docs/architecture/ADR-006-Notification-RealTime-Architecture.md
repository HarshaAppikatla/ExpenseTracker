# ADR-006: Notification & Real-Time Architecture

## 1. Status
*   **ADR Number:** ADR-006
*   **Status:** ACCEPTED
*   **Version:** 1.0
*   **Sprint:** 07
*   **Supersedes:** None
*   **Depends On:** [ADR-005 Shared Expenses and Settlements](file:///E:/ExpenseTrack/docs/architecture/ADR-005-Shared-Expense-Settlement-Architecture.md)

---

## 2. Context
As ExpenseFlow transitions to a collaborative group and trip workspace platform, users require real-time awareness of system mutations (such as newly posted expenses, generated settlements, payment confirmations, and raised disputes).

Synchronously generating and delivering notifications within the core transaction boundary of another context (e.g., inside `ExpenseCommandService`) violates Bounded Context boundaries under Domain-Driven Design (DDD), couples transaction success to notification delivery stability, and degrades API response latency. Furthermore, long-lived client updates require a lightweight, one-way push channel without the overhead and state management complexity of bi-directional WebSockets.

---

## 3. Decision Drivers
- Preserve bounded context independence.
- Do not increase transaction latency.
- Deliver notifications in near real time.
- Keep infrastructure simple.
- Reuse existing Spring ecosystem.
- Support future horizontal scaling.

---

## 4. Decision

### 4.1 Bounded Context Separation
- Establish a dedicated **Notification Bounded Context** that isolates notification lifecycle logic.
- The Notification Context must expose only REST APIs and consume only published events. Other contexts must not query `NotificationRepository` directly.
- The Notification Context must never call Expense, Group, or Trip services directly.
- Conversely, core contexts (Expense, Group, Trip) must never depend on or call the Notification Context.

### 4.2 Event-Driven Architecture
- Bounded contexts communicate exclusively via transactional domain events (e.g. `ExpensePostedEvent`, `SettlementDisputedEvent`).
- Events are captured using Spring's `@TransactionalEventListener` configured for the `AFTER_COMMIT` phase, ensuring notifications are only generated if the originating transaction persists successfully.
- SSE delivery is decoupled from notification persistence by publishing an asynchronous `NotificationCreatedEvent` to be handled out-of-band.

```
[Expense Context] 
       │ (Transaction Commits)
       ▼
[ExpensePostedEvent]
       │
       ▼ (AFTER_COMMIT Listener)
[Notification Context] ──► [Notification Aggregate] (Persist to DB)
                                  │
                                  ▼ (Publish NotificationCreatedEvent)
                           [SsePublisher] (Async Dispatch via Emitter)
```

### 4.3 Notification Aggregate Design
- **Aggregate Root**: Model the aggregate as `Notification` (persistence implementation: `NotificationEntity` mapping the notifications schema).
- **Immutability**: All properties (except for status and deletion flags) are immutable after creation.
- **Value Object**: Contextual metadata (IDs, amounts, currencies, and version) is modeled as a type-safe `NotificationPayload` value object containing a `version` field for schema evolution.
- **JPA Converter**: The `NotificationPayload` is serialized and deserialized to TEXT/JSON format in the database via a JPA `AttributeConverter`.

### 4.4 SSE Stream Delivery & Connection Management
- Live notifications are pushed to clients over a long-lived HTTP Server-Sent Events (SSE) channel at `/api/v1/notifications/stream`.
- **Registry**: Connection streams are managed in a thread-safe `SseEmitterRegistry` map (mapping active user IDs to emitter arrays).
- **Heartbeats**: A Spring `TaskScheduler` broadcasts a periodic heartbeat event (`event: heartbeat`, `data: {}`) every 30 seconds to prevent connection timeouts by browser clients or intermediate proxies (e.g., Nginx, Cloudflare).

### 4.5 Security Model
- SSE handshake connections are authenticated via the standard Spring Security filter chain.
- Authentication tokens must be supplied via standard HTTP cookies (`HttpOnly` JWT token) or standard Authorization headers (`Bearer` JWT token). Query string parameters for auth tokens are strictly prohibited.

### 4.6 Read Model & Database Optimization
- **Retention**: User dismissals trigger a soft delete (`is_deleted = true`). Physical purge of soft-deleted notifications older than 90 days is deferred to Sprint 08.
- **Index**: To optimize unread counts and paginated queries, the migration `V11__notifications.sql` must create a composite index on `(user_id, status, created_at DESC)`.

---

## 5. Alternatives Considered

### Option A — Synchronous NotificationService calls
Rejected because:
- Couples bounded contexts.
- Increases transaction duration.
- Notification failures impact expense creation.

### Option B — Spring ApplicationEvents + SSE (Selected)
Advantages:
- Loose coupling.
- Transaction safety.
- Easy to test.
- Simple deployment.
- SSE supports automatic reconnect out-of-the-box and works over standard HTTP.

### Option C — WebSockets
Rejected because:
- Bidirectional communication is unnecessary since notification delivery is strictly server $\rightarrow$ client.
- More infrastructure complexity.
- Higher connection management overhead.

---

## 6. Non-Goals
The following features are explicitly out of scope for Sprint 07:
- Push notifications (APNS, FCM).
- Email notifications.
- SMS alerts.
- Mobile notifications.
- Cross-device read status synchronization.

---

## 7. Consequences
- **Decoupling**: Complete domain boundaries isolation. Concurrency bugs or delivery errors in the SSE stream cannot block expense or settlement creations.
- **Response Performance**: Persistence transactions remain fast because notification processing and broadcasting run asynchronously out-of-band.
- **Connection Stability**: Periodic heartbeats prevent premature stream teardowns by network middleware.
- **Future Scale**: When ExpenseFlow scales horizontally, `NotificationCreatedEvents` can be broadcasted via Redis Pub/Sub or Kafka. The current single-node registry remains simple.

---

## 8. Architectural Constraints
- **Directional Dependencies**: Bounded context dependencies must strictly flow inwards. Notification Context must never import Expense Context classes.
- **Publishing Rules**: Repositories must never publish events. Only application command services are permitted to publish domain events.
- **Controller Separation**: SSE controllers must remain simple entry channels and contain zero business rules.
- **Payload Immutability**: `NotificationPayload` fields are immutable.
