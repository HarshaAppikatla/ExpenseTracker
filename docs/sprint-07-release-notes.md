# Release Notes — Version v0.10.0-realtime-notifications

This release introduces real-time notification capabilities to **ExpenseFlow** using Server-Sent Events (SSE), decoupled event listeners, and dynamic UI dashboard components.

---

## 🚀 Highlights
* **Real-Time Delivery Engine**: Live push notifications dispatched instantly to browser client tabs on event commits.
* **Collaboration Dashboards**: Interactive metrics highlighting pending group settlements, budget warnings, and recent alerts.

---

## 🌟 Major Features
1. **Server-Sent Events (SSE) Stream**: High-performance HTTP stream connection endpoint at `GET /api/v1/notifications/stream`.
2. **Notification Drawer**: Page-scrolled, right-sliding drawer sidebar listing paginated notifications.
3. **Pulsing Indicator Badge**: Visual header bell badge indicating active unread counts.
4. **Aggregate Settlement Card**: Summarizes user owed/owe totals across all active groups.
5. **Dynamic Budget Alert Banner**: Highlight card at the top of the dashboard displaying warning levels (>80%) and limit-exceeded errors.

---

## 🏗️ Architecture Changes (ADR-006)
* **Decoupled Bounded Context**: Created the `com.expenseflow.notification` package. The context is isolated and depends on no external services.
* **Event-Driven Propagation**: Implemented `@TransactionalEventListener(AFTER_COMMIT)` listeners that capture domain events and trigger notification persistence.
* **CQRS-lite Service Separation**: Split mutations (`NotificationCommandService`) and reads (`NotificationQueryService`).
* **Connection Lifecycle Registry**: `SseEmitterRegistry` manages active emitter channels with automatic eviction and a 10-connection limit.
* **HTTP/Cookie Auth Fallback**: `JwtAuthenticationFilter` matches HttpOnly cookies (`JWT`) to authenticate native EventSource browser connections.

---

## ⚠️ Breaking Changes
* **None**: This release introduces a new bounded context with zero changes to existing database tables or core APIs.

---

## ⚡ Performance Improvements
* **Non-Blocking Publishing**: Event notifications are broadcasted asynchronously (`@Async`) to the registry, preventing thread blockages on the core transaction runtime.
* **Keep-Alive Heartbeats**: Heartbeats are scheduled via a background `ThreadPoolTaskScheduler` thread pool, maintaining open socket connections without load spikes.

---

## 🧪 Testing Summary
* **Test Suites Run**: 38 tests executed, 0 failures.
* **Slice Tests**: Mocked integration controller tests (`@WebMvcTest`) verify handshake and message transmission sequences without requiring Docker-based Testcontainers.
* **Concurrency Validation**: Registry thread safety verified for multi-tab users.

---

## 📌 Known Limitations
* **Local Testcontainers Execution**: Full compilation checks (`mvn verify`) require a running local Docker engine to satisfy integration tests for other bounded contexts.
* **Max Connections Limit**: Users are limited to 10 concurrent active tabs (FIFO eviction cleans up older connections).

---

## ⏩ Next Sprint (Sprint 08)
* **File Management & Production Readiness**: Implementing receipt upload APIs, local/cloud storage abstraction layers, drag-and-drop frontend UI, file validation (size, MIME type), and cleanup policies.
