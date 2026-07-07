# Sprint 07 — Real-Time Notifications & User Experience Polish

Version: 1.4

Estimated Duration: 10–14 Days

Release Version: v0.10.0-realtime-notifications

Status: Frozen & Ready for Implementation

---

## Sprint Goal

Sprint 07 shifts focus toward making **ExpenseFlow** feel collaborative, alive, and production-grade by introducing a decoupled, event-driven **Notification Bounded Context** via Server-Sent Events (SSE) and polishing the dashboard with key collaborative feeds and alerts.

The primary objectives are:

1. **Establish Notification Context**: Create the backend database schema, entity aggregates, and services for tracking user-scoped notification alerts.
2. **Decoupled Domain Event Propagation**: Expense and settlement domains will publish transactional domain events. A dedicated event listener in the notification context will catch these events (`AFTER_COMMIT`) and create notifications, keeping contexts decoupled.
3. **Structured Notification Value Objects & Immutability**: Model the notification payload as an immutable `NotificationPayload` value object, persisted to the database via a JPA AttributeConverter. Keep `NotificationEntity` immutable except for state transitions.
4. **Real-Time Delivery (SSE)**: Implement a thread-safe connection registry and stream endpoint to broadcast live JSON event feeds. Authenticate SSE requests using standard HTTP cookies or authorization headers rather than insecure query parameters.
5. **Heartbeats & Last-Event-ID**: Support periodic heartbeats (every 30 seconds) via a Spring `TaskScheduler` to maintain proxy connections and note `Last-Event-ID` support in ADR-006 for reconnection recovery.
6. **Notification UI**: Build a polished notification center drawer, header bell indicator, and unread counters in the frontend layout.
7. **Dashboard Polish**: Extend the Dashboard page with visual notification feeds, active debt summaries, and upcoming recurring bill alerts.
8. **Strict Validation & Concurrency Tests**: Verify backend thread-safety under heavy connection loads and ensure clean emitter removals.

---

## Bounded Context Boundaries & Architectural Constraints

This sprint operates at the boundary of the **Notification Bounded Context**, **Group Context**, and **Collaborative Expense Context**.

### Architectural Constraints
- **Directional Dependencies**: The Expense Bounded Context must never depend on the Notification Bounded Context.
- **Decoupled Context Access**: The Notification Context must not call Expense/Group services directly; it must only react to published domain events.
- **Domain Decoupling**: Repositories must not publish events. Only application command services are permitted to publish domain events.
- **SSE Domain Separation**: The SSE controller layer must not contain business logic.
- **Aggregate Immutability**: `NotificationPayload` is strictly immutable. `NotificationEntity` core properties (title, payload, category, type) are immutable after creation; only state transitions (`status` modifications) are permitted.

---

## Architecture Flow (Event-Driven Notification Stream)

The cross-context notification propagation pipeline operates as follows:

```
Expense Context (or Settlement)
        │
        ▼
ExpensePostedEvent / SettlementDisputedEvent (published on commit)
        │
  (AFTER_COMMIT)
        ▼
Notification Bounded Context (NotificationEventListener)
        │
        ▼
NotificationCommandService (creates NotificationEntity)
        │
        ▼
NotificationRepository (persisted to database)
        │
        ▼
Publish NotificationCreatedEvent (Asynchronous SSE Dispatch)
        │
        ▼
SsePublisher (broadcasts event to active user emitters + heartbeats)
        │
        ▼
Frontend EventSource (receives event payload)
        │
        ▼
React Query Cache (invalidates 'notifications' cache)
        │
        ▼
Notification Bell / Drawer updates UI + triggers react-hot-toast
```

---

## Sprint Scope

### 1. Notification Aggregate & Schema (Backend)
- **Database Migration (`V11__notifications.sql`)**:
  - `notifications` table containing: `id`, `user_id` (recipient), `title`, `message`, `status` (`READ`, `UNREAD`), `priority` (`LOW`, `NORMAL`, `HIGH`, `CRITICAL`), `category` (`SYSTEM`, `GROUP`, `TRIP`, `EXPENSE`, `SETTLEMENT`), `payload` (JSON string mapped as TEXT/JSON), `is_deleted` (boolean soft delete status), and auditing timestamps.
  - **Indexes**: Includes a composite index on `(user_id, status, created_at DESC)` to optimize unread count queries.
- **Aggregate Root**: `NotificationEntity` keeping core fields immutable after creation, managing only read/unread transitions and soft-deletion flags.
- **Value Object**: `NotificationPayload` mapped using `AttributeConverter<NotificationPayload, String>` to serialize/deserialize metadata.
- **Retention Policy**: Notifications are marked as deleted (soft delete) when dismissed by a user, and physically deleted by a retention cleanup job (scheduled for Sprint 08 implementation).

### 2. Event-Driven Workflow & Value Mappings

#### Event $\rightarrow$ Notification Mapping
| Domain Event | Notification Category | Notification Priority |
|---|---|---|
| `ExpensePostedEvent` | `EXPENSE` | `NORMAL` |
| `SettlementGeneratedEvent` | `SETTLEMENT` | `NORMAL` |
| `SettlementConfirmedEvent` | `SETTLEMENT` | `NORMAL` |
| `SettlementDisputedEvent` | `SETTLEMENT` | `HIGH` |
| `MemberJoinedEvent` | `GROUP` | `LOW` |

### 3. REST & SSE Endpoints
- **CRUD REST Endpoints**:
  - `GET /api/v1/notifications?page=0&size=20&sort=createdAt,desc` (paginated read feed)
  - `GET /api/v1/notifications/unread-count` (total unread count)
  - `GET /api/v1/notifications/latest` (quick view of the latest 5 items)
  - `POST /api/v1/notifications/{id}/read` (mark read)
  - `POST /api/v1/notifications/read-all` (mark all read)
  - `DELETE /api/v1/notifications/{id}` (soft-delete notification)
- **SSE Stream Endpoint**:
  - `GET /api/v1/notifications/stream` returning `text/event-stream`.
  - **Auth**: Resolves user context using JWT cookies/headers inside security filters. No query parameters for auth.
  - **Heartbeats**: Periodic heartbeat event (`event: heartbeat`, `data: {}`) pushed every 30 seconds using Spring `TaskScheduler` to maintain connection stability.

### 4. Notification UI (Frontend)
- **Custom Hooks**:
  - `useNotifications` (paginated query)
  - `useUnreadCount`
  - `useNotificationMutations` (read, read-all, delete)
- **UI Components**:
  - **Header Notification Bell**: Display count badge with pulsing effects, showing a dropdown layout on click.
  - **Notification Center Drawer**: Sidebar drawer listing notification feeds with mark-read, delete, and link-navigation triggers.
  - **Live Toast Notices**: Auto-triggering popups when receiving live SSE messages.

### 5. Dashboard Enhancements
- **Notification Feed Widget**: Render recent notifications on the dashboard.
- **Outstanding Settlement Card**: Summarize net pending debts ("You owe ₹XX / You are owed ₹XX").
- **Budget Warnings**: Highlight categories exceeding defined limits.
- **Upcoming Recurring Expenses**: Display list of recurring billing items due within the next 7 days.
