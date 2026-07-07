# ADR-005: Shared Expense & Settlement Architecture

## 1. Status

| Field | Value |
|---|---|
| **ADR Number** | ADR-005 |
| **Status** | ACCEPTED & FROZEN |
| **Version** | 3.0 |
| **Sprint** | 06 |
| **Applies To** | v0.9.x |
| **Introduced** | Sprint 06 |
| **Scheduled Review** | Sprint 09 (Analytics) |
| **Supersedes** | None |
| **Depends On** | ADR-001 (Group), ADR-002 (Trip), ADR-003 (Expense), ADR-004 (Money & Currency) |

Any future behavioral change (e.g., cross-currency support, partial settlements) must be introduced through a versioned update to this ADR or a new ADR — never through ad hoc implementation changes.

---

## 2. Context

Until Sprint 05, Groups and Trips exist as organizational workspaces. They manage memberships, timelines, and collaborative planning, but they carry no financial obligations.

Sprint 06 introduces shared financial state for the first time:

- A group member pays for an expense on behalf of others.
- The expense is split across participants using one of four strategies.
- Over time, each member accumulates a net financial position within the group or trip.
- The settlement engine computes the minimum set of payments required to zero all balances.

This introduces substantially more domain complexity than any prior sprint:

- Financial records carry legal and audit implications — they may never be deleted.
- Split calculations must be deterministic and reproducible.
- Settlement generation must be idempotent to prevent duplicate debt records.
- Posted expenses must become immutable to preserve the integrity of settled balances.
- Concurrent modifications to shared financial state must be prevented.
- Authorization must be enforced at the service layer, not just the controller.

A consistent, frozen architectural blueprint is required before implementation begins.

This ADR defines the canonical architecture for the **Shared Expense** and **Settlement** bounded contexts in ExpenseFlow.

---

## 3. Architectural Decisions

The following decisions govern the entire Sprint 06 implementation. No implementation detail may contradict these decisions without a new ADR.

### 3.1 Shared Expense is an Aggregate Root
`SharedExpenseEntity` is the aggregate root of the Shared Expense domain. All modifications to splits and participants must pass through this root. No external context may directly write to `shared_expense_splits` or `shared_expense_participants` tables.

### 3.2 Settlement is a Separate Aggregate
`SettlementEntity` is an independent aggregate root. It references `SharedExpense` by ID only. The Settlement bounded context reads expense split data through `SharedExpenseQueryService` and never accesses the shared expense aggregate's repositories directly.

### 3.3 CQRS-Lite Service Separation
Command services (mutations) are separated from query services (reads). Query services are annotated `@Transactional(readOnly = true)`.

### 3.4 Strategy Pattern for Split Calculations
Split logic is encapsulated behind the `SplitCalculator` interface. New split types require only a new enum value, a new implementation, and a factory registration. No existing calculator is modified. The Open/Closed Principle is preserved.

### 3.5 Domain Value Objects Over Primitives
All financial quantities are represented as domain value objects, never raw primitives. This prevents primitive obsession and centralizes validation. See Section 4 for the full catalogue.

### 3.6 Domain Events Published After Transaction Commit
Domain events are published only after successful transaction commit. A rollback produces no event. Events are immutable facts and are never published from query services.

### 3.7 Soft Deletes Only
Financial records are never hard deleted. All entities use soft delete columns (`is_deleted`, `deleted_at`, `deleted_by`).

### 3.8 Optimistic Locking on Mutable Aggregates
Both `SharedExpenseEntity` and `SettlementEntity` apply JPA `@Version` optimistic locking. A concurrent modification that violates the version check returns HTTP 409 to the client, which must reload and retry.

### 3.9 Posted Expense Immutability
Once a `SharedExpenseEntity` transitions from `DRAFT` to `POSTED`, its financial fields are permanently frozen. Enforced at the aggregate level before any persistence call.

### 3.10 Group Currency Enforcement
All split amounts must use the group's declared default currency. The `CurrencyConsistencyPolicy` value object enforces this at creation time. Cross-currency splits are not supported in this version.

### 3.11 One Repository Per Aggregate Root
There is exactly one repository per aggregate root. Child entities within an aggregate boundary (splits, participants, settlement history) are accessed only through their aggregate root. No standalone repository exists for child entities.

### 3.12 UTC Timestamps Throughout
All timestamps are stored in UTC in the database. Conversion to the user's local timezone is the responsibility of the presentation layer only.

### 3.13 Transaction Isolation Level
All command service transactions use `Isolation.READ_COMMITTED`. This prevents dirty reads while allowing non-repeatable reads, which is acceptable because optimistic locking (`@Version`) handles concurrent modification conflicts at the version level rather than at the isolation level.

```java
@Transactional(isolation = Isolation.READ_COMMITTED)
public SharedExpenseDto createExpense(...) { ... }
```

Query services use Spring's default (`READ_COMMITTED`) with `readOnly = true`. A stricter isolation level (e.g., `REPEATABLE_READ`) is not required and would reduce throughput unnecessarily.

---

## 4. Domain Value Objects

Primitive obsession is explicitly prohibited. The following value objects protect the domain model.

### 4.1 Catalogue

| Value Object | Wraps | Validation Enforced |
|---|---|---|
| `Money` | `BigDecimal amount` + `CurrencyCode` | Non-negative, 2 decimal places, non-null currency |
| `CurrencyCode` | `String` (ISO 4217) | 3-character uppercase code, must be in allowed set |
| `SplitAmount` | `BigDecimal` | Non-negative, max 2 decimal places |
| `Percentage` | `BigDecimal` | 0.00 – 100.00 inclusive, 2 decimal places |
| `ShareCount` | `int` | Positive integer, minimum 1 |
| `SettlementAmount` | `BigDecimal` | Positive, 2 decimal places |
| `ExpenseStatus` | `enum` | `DRAFT`, `POSTED`, `CANCELLED`, `SETTLED` |
| `SettlementStatus` | `enum` | `PENDING`, `CONFIRMED`, `DISPUTED` |
| `SplitType` | `enum` | `EQUAL`, `EXACT`, `PERCENTAGE`, `SHARES` |
| `ParticipantRole` | `enum` | `PAYER`, `PARTICIPANT` |

### 4.2 Construction Rule

All value objects self-validate upon construction. Invalid construction must throw a **domain-specific exception** immediately — never a raw `IllegalArgumentException` and never at the persistence layer.

| Value Object | Exception on Invalid Construction |
|---|---|
| `Money` | `MoneyValidationException` |
| `CurrencyCode` | `CurrencyValidationException` |
| `SplitAmount` | `SplitValidationException` |
| `Percentage` | `PercentageValidationException` |
| `ShareCount` | `SplitValidationException` |

### 4.3 Money Equality

`Money` equality is determined by currency code and numeric value, independent of scale.

```text
Money(INR, 100.00) == Money(INR, 100.0)  →  true
Money(INR, 100.00) == Money(USD, 100.00) →  false
Money(INR, 100.00) == Money(INR, 99.99)  →  false
```

`BigDecimal.compareTo()` must be used for amount comparison, not `equals()`, to ensure scale-independent equality.

---

## 5. Aggregate Boundaries & Ownership

### 5.1 Ownership Map

```text
Group
 ├── Members          (owned by Group aggregate — ADR-001)
 ├── Trips            (owned by Trip aggregate — ADR-002)
 └── Shared Expenses  (owned by Shared Expense aggregate — this ADR)

Shared Expense
 ├── Splits           (owned — accessed only through SharedExpense root)
 └── Participants     (owned — accessed only through SharedExpense root)

Settlement
 └── Settlement History  (owned — accessed only through Settlement root)
```

### 5.2 Explicit Ownership Tables

**Shared Expense Aggregate**
```text
Owns:
  ✓ amount (Money)
  ✓ title, description
  ✓ payer (paidByUserId)
  ✓ participants (SharedExpenseParticipant)
  ✓ splits (SharedExpenseSplit)
  ✓ expense status lifecycle

Does NOT own:
  ✗ settlements
  ✗ settlement calculation
  ✗ group membership
  ✗ user profile
  ✗ trip lifecycle
```

**Settlement Aggregate**
```text
Owns:
  ✓ pending settlement records
  ✓ payment confirmation
  ✓ dispute records
  ✓ settlement history (append-only log)

Does NOT own:
  ✗ shared expenses
  ✗ split calculation logic
  ✗ net balance state (computed on demand)
  ✗ group membership
```

### 5.3 Cross-Aggregate Reference Policy

Aggregates reference each other by ID only. JPA entity objects from one aggregate are never embedded or injected into another.

| Reference | Field Type |
|---|---|
| `SharedExpense → Group` | `groupId` (UUID) |
| `SharedExpense → Trip` | `tripId` (UUID, nullable) |
| `SharedExpense → User (payer)` | `paidByUserId` (UUID) |
| `SharedExpenseSplit → User` | `userId` (UUID) |
| `Settlement → Group` | `groupId` (UUID) |
| `Settlement → Trip` | `tripId` (UUID, nullable) |
| `Settlement → User (debtor)` | `fromUserId` (UUID) |
| `Settlement → User (creditor)` | `toUserId` (UUID) |

### 5.4 Consistency Boundaries

- All modifications to `SharedExpenseSplit` and `SharedExpenseParticipant` must occur through the `SharedExpense` root within a single transaction.
- All modifications to `SettlementHistory` must occur through the `Settlement` root.
- No external bounded context may directly write to any child entity table.

---

## 6. Domain Service Classification

Following DDD terminology, responsibilities are classified into three layers:

### Aggregate (invariant enforcement)
- `SharedExpenseEntity` — owns business rules and lifecycle transitions
- `SettlementEntity` — owns settlement lifecycle and history

### Domain Services (pure calculation, no persistence)
- `SplitCalculator` (interface + implementations) — split amount calculation
- `NetBalanceCalculator` — computes net financial position per user
- `DebtMinimizationSolver` — greedy settlement graph algorithm
- `CurrencyConsistencyPolicy` — validates currency alignment

Domain services are stateless, side-effect-free, and fully unit-testable in isolation.

### Application Services (orchestration + persistence)
- `SharedExpenseCommandService` — orchestrates creation, posting, cancellation
- `SettlementCommandService` — orchestrates generation, payment, disputes
- `SharedExpenseQueryService` — read-only projections
- `SettlementQueryService` — read-only projections

Application services coordinate domain services and repositories within a transaction boundary. They do not contain business logic.

---

## 7. Transaction Boundaries

Each aggregate modification executes within exactly one transactional boundary. Cross-aggregate operations are never part of the same database transaction.

### Create Expense
```text
@Transactional
  │
  ├── Validate membership (GroupQueryService — read-only)
  ├── Invoke SplitCalculatorFactory.resolve(splitType)
  ├── Invoke calculator.calculate(amount, inputs)
  ├── Persist SharedExpenseEntity (with splits and participants)
  └── Commit
        │
        └── Publish SharedExpenseCreatedEvent  ← after commit only
```

### Post Expense
```text
@Transactional
  │
  ├── Load SharedExpenseEntity (with @Version check)
  ├── Assert status == DRAFT → throw EXPENSE_003 if not
  ├── Set status = POSTED, freeze immutable fields
  └── Commit
        │
        └── Publish SharedExpensePostedEvent  ← after commit only
```

### Generate Settlements
```text
@Transactional
  │
  ├── Load all POSTED expense splits for group/trip (read-only)
  ├── Compute net balances (NetBalanceCalculator)
  ├── Solve minimum debt graph (DebtMinimizationSolver)
  ├── Upsert settlement records (idempotent — see Section 10)
  └── Commit
        │
        └── Publish SettlementsGeneratedEvent  ← after commit only
```

### Mark as Paid
```text
@Transactional
  │
  ├── Load SettlementEntity (with @Version check)
  ├── Assert fromUserId == currentUser → throw SETTLEMENT_002 if not
  ├── Assert status == PENDING → throw SETTLEMENT_003 if not
  ├── Set status = CONFIRMED, settled_at = now() [UTC]
  ├── Mark related splits as is_settled = true
  └── Commit
        │
        └── Publish SettlementPaidEvent  ← after commit only
```

---

## 8. State Machines

### 8.1 Shared Expense Lifecycle

```text
DRAFT
  │
  ├──(post)──► POSTED
  │               │
  │               ├──(cancel)──► CANCELLED
  │               │
  │               └──(settle)──► SETTLED  [system-triggered]
  │
  └──(delete)──► [SOFT DELETED]  (only from DRAFT or CANCELLED)
```

**Behavioral Rules per Status:**

| Status | Editable | In Settlements | Deletable | Cancellable |
|---|---|---|---|---|
| `DRAFT` | ✅ | ❌ | ✅ | ❌ |
| `POSTED` | ❌ | ✅ | ❌ | ✅ |
| `CANCELLED` | ❌ | ❌ | ✅ | ❌ |
| `SETTLED` | ❌ | ❌ | ❌ | ❌ |

Any attempt to mutate a `POSTED`, `CANCELLED`, or `SETTLED` expense must throw `InvalidExpenseStateException` → HTTP 409 → error code `EXPENSE_003`.

**Posted Expense Immutability Rule:**

Once an expense transitions to `POSTED`, the following fields become permanently immutable:

- `amount` cannot change
- `participants` cannot change
- `splitType` cannot change
- `paidByUserId` (payer) cannot change

Only cancellation is permitted after posting.

### 8.2 Settlement Lifecycle

```text
PENDING
   │
   ├──(pay → debtor confirms)──► CONFIRMED
   │
   └──(dispute → creditor raises)──► DISPUTED
                                         │
                                         └──(resolve)──► CONFIRMED
```

Any transition not listed above must throw `InvalidSettlementStateException` → HTTP 409 → error code `SETTLEMENT_003`.

---

## 9. Split Strategy Architecture

### 9.1 Interface Contract

```java
public interface SplitCalculator {
    List<SplitResult> calculate(Money totalAmount, List<SplitInput> inputs);
}
```

### 9.2 Implementations

| Strategy | Class | Logic |
|---|---|---|
| `EQUAL` | `EqualSplitCalculator` | Divides total evenly. Distributes 1-cent remainder to first participant. |
| `EXACT` | `ExactSplitCalculator` | Validates provided `SplitAmount` values sum to total within 1-cent tolerance. |
| `PERCENTAGE` | `PercentageSplitCalculator` | Validates `Percentage` values sum to 100. Converts to amounts. Distributes remainder. |
| `SHARES` | `SharesSplitCalculator` | Computes proportional amounts from `ShareCount` values. Distributes remainder. |

### 9.3 Rounding Policy

All split calculations use `HALF_UP` rounding to 2 decimal places. Residual cents are assigned to the first participant to ensure the total is always exactly preserved.

### 9.4 Extensibility Rule

New split types may only be added by:
1. Adding a new value to the `SplitType` enum.
2. Implementing a new class conforming to `SplitCalculator`.
3. Registering the new class in `SplitCalculatorFactory`.

Existing implementations must never be modified for this purpose.

---

## 10. Settlement Algorithm

### 10.1 Net Balance Calculation

`NetBalanceCalculator` scans all `POSTED` expense splits in a group or trip and computes the net financial position per member:

```text
For each POSTED expense:
  payer receives credit:    netBalance[payer] += amount
  each participant owes:    netBalance[user]  -= splitAmount

Result:
  Positive balance → user is owed money (creditor)
  Negative balance → user owes money (debtor)
  Zero balance     → user is settled
```

### 10.2 Debt Minimization

`DebtMinimizationSolver` uses a greedy algorithm to produce the minimum number of settlement transactions. Complexity: O(n log n).

```text
1. Partition users into creditors (balance > 0) and debtors (balance < 0).
2. Sort creditors descending, debtors descending by absolute value.
3. While both lists are non-empty:
   a. Match largest creditor C with largest debtor D.
   b. amount = min(C.balance, |D.balance|)
   c. Record settlement: (D → C, amount)
   d. C.balance -= amount
   e. D.balance += amount
   f. Remove entries where balance == 0.
4. Output: minimum list of (fromUserId, toUserId, amount) pairs.
```

### 10.3 Idempotency Rule

Settlement generation must be deterministic and idempotent.

Running `generateSettlements` multiple times without any intervening expense changes must:
- Produce identical `(from, to, amount)` settlement pairs.
- Not create duplicate settlement records.

**Upsert Strategy:** Before persisting each computed pair, query for an existing `PENDING` settlement with matching `(fromUserId, toUserId, groupId, tripId)`. If found: update `amount` in place. If not found: insert a new record. `CONFIRMED` and `DISPUTED` records are never overwritten.

### 10.4 Settlement Regeneration Triggers

Settlement regeneration should be triggered only after events that materially alter member balances:

| Trigger | Reason |
|---|---|
| `POST /expenses/{id}/post` — expense posted | New POSTED expense changes balances |
| `POST /expenses/{id}/cancel` — expense cancelled | POSTED expense removed from calculations |
| `POST /settlements/{id}/pay` — payment confirmed | Settled splits must be excluded from future recalculations |
| `POST /settlements/generate` — manual trigger | Explicit admin request |

Regeneration is **not** triggered by:
- Editing a DRAFT expense (DRAFT expenses are excluded from calculations)
- Viewing any data (query operations never trigger generation)
- Any non-expense domain event

### 10.5 Performance Targets

| Operation | Target |
|---|---|
| Settlement generation (100 members, 1,000 expenses) | < 1 second |
| Settlement Summary API (`GET /settlements`) | < 200ms |
| Expense List API (`GET /expenses`) | < 300ms |
| Expense Detail API (`GET /expenses/{id}`) | < 150ms |
| Dashboard-level balance summary | < 500ms |

These targets must be verified manually before the sprint Definition of Done is signed off.

---

## 11. Core Invariants

Every developer and pull request must preserve the following rules. Violations must be caught at the aggregate level before any persistence call.

### 11.1 Membership Invariants

1. The expense payer must be an active member of the parent group at creation time.
2. All expense participants must be active members of the same parent group.
3. When `tripId` is provided, the payer and all participants must also be active trip participants.

### 11.2 Financial Invariants

4. The sum of all split amounts must equal the expense total within ±0.01 (1-cent tolerance).
5. For `PERCENTAGE` splits, the sum of all percentages must equal exactly `100.00`.
6. For `SHARES` splits, at least one participant must have a `ShareCount` > 0.
7. All split amounts must use the group's declared default currency (`CurrencyConsistencyPolicy`).

### 11.3 Lifecycle Invariants

8. Only `DRAFT` expenses may have their amount, participants, split strategy, or payer modified.
9. Only `POSTED` expenses are included in settlement calculations.
10. Only `POSTED` expenses may be cancelled.
11. Only `DRAFT` and `CANCELLED` expenses may be soft deleted.
12. Financial records (expenses, splits, settlements) are never hard deleted.

### 11.4 Settlement Invariants

13. Settlement records are only generated from `POSTED` expenses.
14. Settlement generation is idempotent — re-running without expense changes produces no duplicate records.
15. Only `PENDING` settlements may be confirmed or disputed.
16. Only `DISPUTED` settlements may be resolved back to `CONFIRMED`.
17. A settlement may only be confirmed by the `fromUser` (the debtor paying).

---

## 12. Authorization Rules

Authorization is enforced at the **service layer**. Controllers must not contain authorization logic.

### Shared Expense Authorization

| Action | Authorized Roles |
|---|---|
| Create expense | Any active group member |
| View expense list | Any active group member |
| View expense detail | Any active group member |
| Edit DRAFT expense | Payer only |
| Post expense | Payer only |
| Cancel POSTED expense | Payer or group Admin/Owner |
| Soft delete expense | Payer or group Admin/Owner |

### Settlement Authorization

| Action | Authorized Roles |
|---|---|
| View settlement summary | Any active group member |
| Generate settlements | Group Owner or Admin |
| Confirm payment (mark as paid) | `fromUser` (debtor) only |
| Dispute settlement | `toUser` (creditor) only |
| Resolve dispute | Group Owner or Admin |

**Rule:** Any authorization violation must throw a `PermissionDeniedException` → HTTP 403 → error code `EXPENSE_009` or `SETTLEMENT_002`.

---

## 13. Validation Layer

Bean validation (input sanity) and business validation (domain rules) are distinct layers with distinct responsibilities.

```text
HTTP Request
     │
     ▼
Controller
     │   ← Bean Validation (@NotBlank, @NotNull, @Min, @Size)
     │     Rejects malformed input with HTTP 400
     ▼
Command Service
     │   ← Authorization check (member? payer? admin?)
     │     Throws PermissionDeniedException → HTTP 403
     ▼
Aggregate / Value Object
     │   ← Business rule validation (split sums, currency, lifecycle)
     │     Throws domain exceptions → HTTP 409 or HTTP 422
     ▼
Repository
     │   ← Unique constraint, FK constraint (last defense only)
     ▼
Database
```

**Rule:** Business validation must never be delegated to the database constraint layer as the primary enforcement mechanism. The aggregate enforces business rules first.

---

## 14. CQRS-Lite, Repository Ownership & Pagination

### Command Services

| Service | Mutations |
|---|---|
| `SharedExpenseCommandService` | `createExpense`, `updateExpense`, `postExpense`, `cancelExpense`, `deleteExpense` |
| `SettlementCommandService` | `generateSettlements`, `markAsPaid`, `disputeSettlement`, `resolveSettlement` |

### Query Services

| Service | Reads |
|---|---|
| `SharedExpenseQueryService` | `getExpensesByGroup`, `getExpensesByTrip`, `getExpenseDetail`, `getMyExpenses` |
| `SettlementQueryService` | `getSettlementSummary`, `getSettlementsByTrip`, `getMySettlements` |

### Repository Ownership Rules

One repository per aggregate root. Child entities are never given a standalone repository.

| Repository | Aggregate Root | Notes |
|---|---|---|
| `SharedExpenseRepository` | `SharedExpenseEntity` | Owns splits and participants |
| `SettlementRepository` | `SettlementEntity` | Owns settlement history |

**Explicitly Forbidden:**
```text
✗ SharedExpenseSplitRepository       (splits belong to SharedExpense aggregate)
✗ SharedExpenseParticipantRepository (participants belong to SharedExpense aggregate)
✗ SettlementHistoryRepository        (history belongs to Settlement aggregate)
```

### Read Model Policy

- Query services expose DTO projections only — JPA entities never leave the service boundary.
- All query service methods annotated `@Transactional(readOnly = true)`.

### Pagination Policy

All list-returning query service methods must accept a `Pageable` parameter and enforce the following defaults:

| Setting | Value |
|---|---|
| Default page size | 20 items |
| Maximum page size | 100 items |
| Default sort | `createdAt DESC` |

Requests exceeding the maximum page size must return HTTP 400.

---

## 15. Domain Events

### Shared Expense Events

| Event | Published When |
|---|---|
| `SharedExpenseCreatedEvent` | Expense created in DRAFT |
| `SharedExpenseUpdatedEvent` | DRAFT expense edited |
| `SharedExpensePostedEvent` | DRAFT → POSTED |
| `SharedExpenseCancelledEvent` | POSTED → CANCELLED |
| `SharedExpenseDeletedEvent` | Expense soft deleted |
| `SplitCalculatedEvent` | Split calculation completed |

### Settlement Events

| Event | Published When |
|---|---|
| `SettlementsGeneratedEvent` | Settlement generation run completes |
| `SettlementPaidEvent` | PENDING → CONFIRMED |
| `SettlementDisputedEvent` | PENDING → DISPUTED |
| `SettlementResolvedEvent` | DISPUTED → CONFIRMED |

### Event Rules

- Events are published only after successful transaction commit.
- A transaction rollback produces no event under any circumstances.
- Events are immutable value objects representing facts that have already occurred.
- Events must never be published from query services.

### Event Delivery Guarantee

Application events are delivered **at-least-once** within the application boundary using Spring's `ApplicationEventPublisher`. Consumers (Notification service in Sprint 08, Analytics in Sprint 09) must therefore be **idempotent** — processing the same event twice must produce the same result as processing it once.

---

## 16. API Idempotency

| Endpoint | Idempotent | Reason |
|---|---|---|
| `POST /expenses` | ❌ No | Creates a new resource each call |
| `PUT /expenses/{id}` | ✅ Yes | Same input produces same result |
| `POST /expenses/{id}/post` | ✅ Yes | Already-POSTED expense is a no-op |
| `POST /expenses/{id}/cancel` | ✅ Yes | Already-CANCELLED expense is a no-op |
| `DELETE /expenses/{id}` | ✅ Yes | Already-deleted is a no-op |
| `POST /settlements/generate` | ✅ Yes | Idempotent upsert — see Section 10.3 |
| `POST /settlements/{id}/pay` | ✅ Yes | Already-CONFIRMED settlement is a no-op |

---

## 17. Concurrency Strategy

Both `SharedExpenseEntity` and `SettlementEntity` apply JPA `@Version` optimistic locking.

### Conflict Resolution Flow

```text
User A loads SharedExpense (version=5)
User B loads SharedExpense (version=5)
     │
     │  User A edits and commits  →  version becomes 6
     │
User B tries to commit  →  version mismatch detected
     │
     ▼
OptimisticLockException
     │
     ▼
HTTP 409 Conflict
     │
     ▼
Client discards stale state, reloads, and retries
```

**Frontend Contract:** The frontend must handle HTTP 409 responses by discarding stale state, reloading the resource, and prompting the user to resubmit their changes.

---

## 18. Time Handling & Audit Fields

### 18.1 UTC Timestamp Policy

All timestamps are stored in UTC in the database. Conversion to the user's local timezone is the sole responsibility of the presentation layer.

```text
Database → UTC
API response → UTC (ISO 8601)
Frontend → converts to user's local timezone for display
```

### 18.2 Mandatory Audit Fields

The following audit fields are required on every aggregate root and supporting entity in this bounded context:

| Field | Type | Description |
|---|---|---|
| `createdAt` | `Instant` (UTC) | When the record was created |
| `createdBy` | `UUID` | User ID who created the record |
| `updatedAt` | `Instant` (UTC) | When the record was last modified |
| `updatedBy` | `UUID` | User ID who last modified the record |
| `deletedAt` | `Instant` (UTC, nullable) | When the record was soft deleted |
| `deletedBy` | `UUID` (nullable) | User ID who soft deleted the record |

These fields must be populated automatically via JPA `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy`, `@LastModifiedBy` auditing annotations backed by `AuditorAware`.

---

## 19. Database Schema & Migration Dependency Order

Flyway migrations must be applied in the following strict order. Each migration depends on tables created by the prior migration.

```text
V8__shared_expenses.sql
  │
  ├── Creates: shared_expenses
  ├── Creates: shared_expense_splits
  ├── Creates: shared_expense_participants
  └── FK dependency: groups (V6), trips (V7), users (V1)
        │
        ▼
V9__settlements.sql
  │
  ├── Creates: settlements
  ├── Creates: settlement_history
  └── FK dependency: shared_expenses (V8), groups (V6), users (V1)
```

V9 must not be applied before V8 is complete. Any reversal of this order will result in FK constraint violations.

### Expected Indexes After Migration

**`shared_expenses` table**

| Index | Columns |
|---|---|
| `idx_shared_expenses_group` | `group_id` |
| `idx_shared_expenses_trip` | `trip_id` |
| `idx_shared_expenses_payer` | `paid_by_user_id` |
| `idx_shared_expenses_status` | `status` |
| `idx_shared_expenses_group_status` | `group_id, status` |

**`shared_expense_splits` table**

| Index | Columns |
|---|---|
| `idx_splits_expense` | `expense_id` |
| `idx_splits_user` | `user_id` |
| `idx_splits_settled` | `is_settled` |

**`settlements` table**

| Index | Columns |
|---|---|
| `idx_settlements_group` | `group_id` |
| `idx_settlements_trip` | `trip_id` |
| `idx_settlements_from_to` | `from_user_id, to_user_id` |
| `idx_settlements_status` | `status` |

---

## 20. Structured Application Error Codes

### Shared Expense Error Codes

| Code | HTTP Status | Meaning |
|---|---|---|
| `EXPENSE_001` | 422 | Invalid or missing expense amount |
| `EXPENSE_002` | 422 | Split amounts do not sum to expense total |
| `EXPENSE_003` | 409 | Cannot mutate a POSTED, CANCELLED, or SETTLED expense |
| `EXPENSE_004` | 422 | Payer is not an active group member |
| `EXPENSE_005` | 422 | Participant is not an active group member |
| `EXPENSE_006` | 422 | Currency mismatch with group default currency |
| `EXPENSE_007` | 422 | Percentage splits do not sum to 100% |
| `EXPENSE_008` | 404 | Expense not found |
| `EXPENSE_009` | 403 | Permission denied |

### Settlement Error Codes

| Code | HTTP Status | Meaning |
|---|---|---|
| `SETTLEMENT_001` | 404 | Settlement not found |
| `SETTLEMENT_002` | 403 | Permission denied — only debtor may confirm |
| `SETTLEMENT_003` | 409 | Invalid settlement state transition |
| `SETTLEMENT_004` | 422 | No POSTED expenses found to generate settlements |

### Reserved Error Code Ranges

Codes are reserved in ranges to prevent renumbering as the application evolves. New codes must be assigned within the range belonging to their bounded context.

| Range | Bounded Context | Sprint Introduced |
|---|---|---|
| `EXPENSE_001 – EXPENSE_099` | Shared Expense domain | Sprint 06 |
| `SETTLEMENT_001 – SETTLEMENT_099` | Settlement domain | Sprint 06 |
| `GROUP_100 – GROUP_199` | Group Management | Sprint 04 |
| `TRIP_200 – TRIP_299` | Trip Management | Sprint 05 |
| `PROFILE_300 – PROFILE_399` | Profile & Account | Sprint 07 |
| `NOTIFICATION_400 – NOTIFICATION_499` | Notifications | Sprint 08 |
| `ANALYTICS_500 – ANALYTICS_599` | Analytics & Reporting | Sprint 09 |
| `SYSTEM_900 – SYSTEM_999` | Infrastructure / Internal | All sprints |

Any new bounded context introduced in a future sprint must register its range in this table before assigning codes.

### Error Response Shape (Spring Boot 3 ProblemDetail — RFC 7807)

```json
{
  "type": "https://expenseflow.io/errors/EXPENSE_003",
  "title": "Invalid Expense State",
  "status": 409,
  "detail": "Cannot mutate a POSTED expense. Only cancellation is permitted.",
  "instance": "/api/v1/groups/{groupId}/expenses/{expenseId}",
  "timestamp": "2026-07-06T12:00:00Z",
  "errorCode": "EXPENSE_003"
}
```

---

## 21. Package & Dependency Boundaries (ArchUnit Guarded)

```text
controller  ──►  service  ──►  repository
controller  ──✗  repository              (FORBIDDEN)
repository  ──✗  repository              (FORBIDDEN)
service     ──✗  controller              (FORBIDDEN)
settlement  ──✗  shared_expense repo     (FORBIDDEN — use QueryService only)
```

ArchUnit tests must enforce all FORBIDDEN rules automatically on every build.

---

## 22. Testing Strategy

### Unit Tests

| Target | What to Verify |
|---|---|
| `EqualSplitCalculator` | Even split, rounding remainder to first participant |
| `ExactSplitCalculator` | Validates sum, rejects if exceeds tolerance |
| `PercentageSplitCalculator` | 100% sum validation, amount conversion |
| `SharesSplitCalculator` | Proportional calculation, zero shares rejection |
| `NetBalanceCalculator` | Net balance correctness for known payer/participant sets |
| `DebtMinimizationSolver` | Minimum transaction count, known deterministic scenarios |
| `SharedExpenseEntity` invariants | Posting locks fields, status transitions, soft delete rules |
| `SettlementEntity` invariants | State machine transitions, unauthorized confirmation rejected |
| `Money` value object | Equality by value (scale-independent), currency mismatch |
| Domain exceptions | `MoneyValidationException`, `PercentageValidationException`, etc. |

### Integration Tests

| Target | What to Verify |
|---|---|
| `SharedExpenseRepository` | CRUD, paginated queries by group/trip/status |
| `SettlementRepository` | Upsert idempotency — re-generate produces no duplicate records |
| Flyway V8, V9 | Schema applies in order, all indexes exist post-migration |
| `SharedExpenseController` | Full API request/response cycle with Spring MockMvc |
| `SettlementController` | Generation, mark as paid, dispute, resolve flows |

### Architecture Tests (ArchUnit)

| Rule | Enforcement |
|---|---|
| No controller → repository | `ArchUnit.noControllerAccessesRepository()` |
| No repository → repository | `ArchUnit.noRepositoryAccessesRepository()` |
| No settlement → expense repository | `ArchUnit.settlementDoesNotAccessExpenseRepository()` |

### Performance Tests

| Scenario | Acceptance Criterion |
|---|---|
| Settlement generation, 100 members, 1,000 expenses | < 1 second |
| Settlement summary API | < 200ms |
| Expense list API (page 1, 20 items) | < 300ms |

---

## 23. Sequence Diagrams

### 23.1 Create Shared Expense

```text
User
 │
 ▼
SharedExpenseController
 │  POST /groups/{groupId}/expenses
 │  ① Bean validation (@NotBlank, @NotNull, @Min)
 │
 ▼
SharedExpenseCommandService
 │  ② Authorization: assert currentUser is group member
 │  ③ Invoke SplitCalculatorFactory.resolve(splitType)
 │  ④ Invoke calculator.calculate(amount, inputs)
 │  ⑤ Assert split sum within 1-cent tolerance
 │  ⑥ Assert currency matches group default
 │
 ▼
SharedExpenseEntity (Aggregate Root)
 │  ⑦ Construct with Money, participants, splits
 │  ⑧ Assert payer is group member
 │  ⑨ Set status = DRAFT
 │
 ▼
SharedExpenseRepository
 │  ⑩ persist() within @Transactional
 │
 ▼
Transaction Commits
 │
 ▼
ApplicationEventPublisher
 │  ⑪ Publish SharedExpenseCreatedEvent
 │
 ▼
HTTP 201 Created → SharedExpenseDto
```

### 23.2 Settlement Generation

```text
Group Admin
 │
 ▼
SettlementController
 │  POST /groups/{groupId}/settlements/generate
 │  ① Authorization: assert currentUser is Owner or Admin
 │
 ▼
SettlementCommandService
 │
 ▼
SharedExpenseQueryService
 │  ② Load all POSTED splits for group/trip (readOnly tx)
 │
 ▼
NetBalanceCalculator
 │  ③ Compute net balance per user
 │
 ▼
DebtMinimizationSolver
 │  ④ Greedy graph — produce minimum (from, to, amount) pairs
 │
 ▼
SettlementCommandService
 │  ⑤ For each pair: upsert PENDING settlement (idempotent)
 │
 ▼
SettlementRepository
 │  ⑥ persist() within @Transactional
 │
 ▼
Transaction Commits
 │
 ▼
ApplicationEventPublisher
 │  ⑦ Publish SettlementsGeneratedEvent
 │
 ▼
HTTP 200 OK → SettlementSummaryDto
```

### 23.3 Mark Settlement as Paid

```text
Debtor (fromUser)
 │
 ▼
SettlementController
 │  POST /settlements/{settlementId}/pay
 │
 ▼
SettlementCommandService
 │  ① Load SettlementEntity (with @Version)
 │  ② Assert currentUser == fromUserId → HTTP 403 if not
 │  ③ Assert status == PENDING → HTTP 409 if not
 │  ④ Set status = CONFIRMED, settled_at = now() [UTC]
 │  ⑤ Mark related splits is_settled = true
 │
 ▼
SettlementRepository
 │  ⑥ persist() within @Transactional
 │
 ▼
Transaction Commits
 │
 ▼
ApplicationEventPublisher
 │  ⑦ Publish SettlementPaidEvent
 │
 ▼
HTTP 200 OK → SettlementDto
```

---

## 24. Consequences

### Advantages

- **Clear aggregate ownership** makes it unambiguous which service owns which data.
- **Domain value objects** eliminate primitive obsession and centralize validation.
- **Domain-specific exceptions** produce clean, structured error responses throughout.
- **Strategy Pattern** allows new split types without modifying existing code.
- **CQRS-lite** reduces service complexity and simplifies read-path optimization.
- **Idempotent settlement generation** makes the API safe to call multiple times.
- **Optimistic locking** prevents silent data corruption under concurrent load.
- **Posted expense immutability** protects historical financial record integrity.
- **Authorization at the service layer** prevents privilege escalation regardless of how the API is called.
- **At-least-once event delivery** with idempotent consumers ensures downstream services stay consistent.
- Each concern (balance calculation, debt minimization, split calculation, event publishing) is independently unit-testable.

### Accepted Trade-offs

- More domain classes than a simple CRUD implementation.
- More services require more unit tests.
- Additional persistence complexity (more tables, more indexes, more joins).
- Idempotent settlement generation requires an upsert check before each persist.
- Aggregate boundaries prevent shortcuts such as direct cross-table writes.
- Optimistic locking requires clients to handle HTTP 409 gracefully.
- At-least-once delivery requires all event consumers to be written idempotently.

These trade-offs are accepted. The correctness, auditability, and security requirements of a financial system justify the additional structure.

---

## 25. Future Considerations

The following capabilities were consciously deferred:

- **Cross-currency settlements** — multi-currency group support with live exchange rate conversion.
- **Partial settlement payments** — pay a portion of a debt rather than the full outstanding amount.
- **Recurring shared expenses** — subscription-style bills that auto-generate on a schedule.
- **Split templates** — save a participant list and strategy preset for reuse.
- **Expense approval workflow** — require admin approval before an expense transitions to POSTED.
- **OCR receipt attachments** — extract amount, merchant, and date from a scanned receipt.
- **AI-assisted split suggestions** — infer participants and amounts from receipt content.
- **Expense dispute workflow** — allow a participant to contest their assigned split before posting.

---

## 26. Sprint 06 Definition of Done — Implementation Gate

Sprint 06 cannot be considered complete until all of the following conditions are verified against this ADR:

- [ ] All 17 core invariants (Section 11) are enforced and covered by unit tests
- [ ] All ArchUnit architectural rules (Section 21) pass on CI
- [ ] All performance targets (Section 10.5) are met and recorded
- [ ] Domain-specific exceptions (Section 4.2) are used throughout — no raw `IllegalArgumentException`
- [ ] `Money` equality uses `BigDecimal.compareTo()` — not `equals()`
- [ ] Settlement regeneration is triggered only by events listed in Section 10.4
- [ ] All list APIs enforce the pagination policy (Section 14)
- [ ] All timestamps are stored in UTC and returned as ISO 8601 (Section 18.1)
- [ ] All audit fields are present on every entity (Section 18.2)
- [ ] Flyway migrations applied in correct order V8 → V9 (Section 19)
- [ ] All database indexes exist post-migration (Section 19)
- [ ] Authorization enforced at service layer for all operations (Section 12)
- [ ] Event delivery guarantee documented to downstream consumers (Section 15)
- [ ] HTTP 409 handled gracefully by frontend for optimistic lock conflicts (Section 17)
- [ ] No implementation contradicts any decision in Section 3
- [ ] ADR-005 referenced in Sprint 06 commit message and release notes

---

## 27. Future Sprint Dependency Roadmap

```text
Sprint 06 (This Sprint — Shared Expense & Settlements)
 └── Introduces SharedExpense and Settlement aggregates

Sprint 07 (Profile & Account Management)
 └── User context only. No dependency on this ADR.

Sprint 08 (Notifications)
 └── Consumes: SharedExpensePostedEvent, SettlementPaidEvent, SettlementsGeneratedEvent
 └── Must implement idempotent consumers (Section 15)

Sprint 09 (Analytics & Reporting)
 └── Reads: SharedExpense and Settlement ledger for trend analysis and export
 └── Scheduled ADR-005 review at Sprint 09

Future (OCR / AI)
 └── Enriches SharedExpense creation with automated receipt parsing

Future (Cross-Currency)
 └── Extends CurrencyCode and Money value objects
 └── Requires versioned update to ADR-005 Section 10 (Settlement Algorithm)
```
