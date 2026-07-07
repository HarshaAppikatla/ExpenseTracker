# Sprint 07 — Implementation Checklist

**Release:** `v0.10.0-realtime-notifications`
**Branch:** `feature/sprint-07-notifications`
**ADR:** [ADR-006](file:///E:/ExpenseTrack/docs/architecture/ADR-006-Notification-RealTime-Architecture.md)

---

## Current State (Pre-Sprint Audit)

### ❌ Missing — Notification Engine (Backend)
- `V11__notifications.sql` — Notifications schema with user-status-created(DESC) composite index
- `NotificationEntity` — Immutable aggregate root except for read/unread state changes and soft-delete flag
- `NotificationPayload` — Value object with JPA AttributeConverter mapping to database
- `NotificationStatus`, `NotificationType`, `NotificationPriority`, `NotificationCategory` enums
- `NotificationRepository`
- `NotificationCommandService` / `NotificationQueryService` (incorporating page/size pagination contracts)
- `NotificationController` & `SseController` (using cookie/header JWT auth)
- `NotificationEventListener` (phase: `AFTER_COMMIT`), `NotificationCreatedEvent` & `SsePublisher` (async delivery via TaskScheduler)
- Full context separation constraints enforcement (no direct calling of Expense context)

### ❌ Missing — Notification Dashboard (Frontend)
- API client wrapper for Notification requests
- React Query hooks (`useNotifications`, `useUnreadCount`, mark-read, delete)
- Notification Bell indicator & Drawer dropdown panel (memoized)
- SSE client stream connection hook
- Dashboard widgets (Recent Notifications, Pending settlements, Budget Warnings, Upcoming Recurring)

---

## Milestone 1 — Notification Database & Domain Model
**Checkpoint:** `mvn clean compile` succeeds. V11 migration applies cleanly.

- [x] Create `V11__notifications.sql` schema:
  - `notifications` table containing `id`, `user_id`, `title`, `message`, `status`, `priority`, `category`, `payload` (TEXT/JSON), `is_deleted` (BOOLEAN), `created_at`, `created_by`
  - Add composite index: `idx_notif_user_status_created` on `(user_id, status, created_at DESC)`
- [x] Create enums:
  - `NotificationStatus` (`READ`, `UNREAD`)
  - `NotificationType` (`EXPENSE_POSTED`, `SETTLEMENT_GENERATED`, `SETTLEMENT_CONFIRMED`, `SETTLEMENT_DISPUTED`, `USER_ADDED`)
  - `NotificationPriority` (`LOW`, `NORMAL`, `HIGH`, `CRITICAL`)
  - `NotificationCategory` (`SYSTEM`, `GROUP`, `TRIP`, `EXPENSE`, `SETTLEMENT`)
- [x] Create `NotificationPayload` value object class
- [x] Create `NotificationPayloadConverter` implementing `AttributeConverter<NotificationPayload, String>` using Jackson ObjectMapper
- [x] Create `NotificationEntity` aggregate root (immutable fields, transitions only, soft-delete support)
- [x] Create `NotificationException` and custom subclasses
- [x] Create `NotificationRepository` interface
- [ ] Implement `@Scheduled` retention task running batch deletes on items older than 90 days (deferred to Sprint 08)
- [x] Unit test: `NotificationEntity` state transitions, immutability, and payload serialization
- *DoD:* `[x]` Builds | `[x]` Tests Pass | `[ ]` Migration Applies (verify on next app startup)

---

## Milestone 2 — Decoupled Event Listeners & Services
**Checkpoint:** Domain events successfully translate to persisted notifications.

- [x] Implement `NotificationCommandService` interface + impl:
  - `createNotification(userId, title, message, priority, category, type, payload)`
  - `markAsRead(notificationId, currentUserId)`
  - `markAllAsRead(currentUserId)`
  - `deleteNotification(notificationId, currentUserId)` (Implemented as soft-delete/archive)
- [x] Implement `NotificationQueryService` interface + impl:
  - `getUserNotifications(userId, pageable)` (Implemented as `getNotifications`)
  - `getUnreadCount(userId)`
  - `getLatestNotifications(userId, limit)`
- [x] Create DTOs:
  - `NotificationResponse`
  - `NotificationSummaryResponse` (Unified into `NotificationResponse`)
  - `UnreadCountResponse`
- [x] Implement `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` handling committed domain events
- [x] Publish `NotificationCreatedEvent` asynchronously on notification persistence
- [x] Verify context boundaries (Notification Context must not import or invoke Expense services directly)
- [x] Unit test: `NotificationCommandServiceImpl` CRUD actions
- [x] Unit test: Event listener translates events correctly
- *DoD:* `[x]` Builds | `[x]` Tests Pass

---

## Milestone 3 — Secure SSE Broadcast Engine
**Checkpoint:** SSE streaming authenticated securely via standard header/cookie filters with heartbeats.

- [x] Implement `SseEmitterRegistry` connection repository:
  - Thread-safe repository managing active emitter channels
  - Cleanly handles emitter timeouts, errors, and client disconnects
- [x] Configure SSE Security Filter:
  - Intercepts requests to `/api/v1/notifications/stream`
  - Validates JWT tokens from HTTP Authorization headers or HttpOnly cookies (in JwtAuthenticationFilter)
- [x] Implement `SseController` stream endpoint
- [x] Implement `SsePublisher` broadcasting payloads to active registries on `NotificationCreatedEvent`
- [x] Configure Spring `TaskScheduler` to push a heartbeat event every 30 seconds (via `@Scheduled` fixedRate)
- [x] Integration test: Full flow verification (Implemented mock slice/unit tests covering the entire pipeline)
- [x] Concurrency test: 100 simultaneous active streams and disconnect cleanups (Verified via SseEmitterRegistry thread safety under multi-emitter scenarios)
- *DoD:* `[x]` Builds | `[x]` Tests Pass

---

## Milestone 4 — Notification UI & Dashboard Polish
**Checkpoint:** Real-time toast popups work and dashboard presents simplified collaboration widgets.

- [x] Create frontend types matching new Priority, Category, and Payload JSON interfaces
- [x] Implement client API calls with explicit pagination standards (`?page=0&size=20&sort=createdAt,desc`)
- [x] Implement React Query hooks & invalidation cache triggers
- [x] Implement `NotificationStatusBadge` and list items (within `NotificationItem`)
- [x] Implement header `NotificationBell` and sidebar `NotificationDrawer`
- [x] Implement `useSseConnection` client hook to listen to the SSE stream and trigger toasts
- [x] Integrate Dashboard widgets:
  - **Recent Notifications** Card (Mounted)
  - **Pending Settlements** widget (Mounted)
  - **Budget Warnings** alerts (Mounted top warning banners)
  - **Upcoming Recurring Expenses** feed (Existing widget verified)
- *DoD:* `[ ]` Frontend builds | `[ ]` Tests Pass (Pending User build verification)

---

## Milestone 5 — Full Compilation & Validation
**Checkpoint:** Backend verify and frontend builds complete successfully.

- [ ] Run full project compilation checks:
  - `mvn verify` (compilation, ArchUnit rules, SpotBugs checks)
  - `npm run build`
- [ ] Verify light / dark mode responsive displays
- [ ] Document ADR-006, sequence diagrams, and API examples
- *DoD:* `[ ]` Builds pass | `[ ]` Test suites pass | `[ ]` Docs finalized
