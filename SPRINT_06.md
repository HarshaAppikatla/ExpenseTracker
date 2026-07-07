# Sprint 06 — Shared Expense Management & Settlements

Version: 1.1

Estimated Duration: 10–14 Days

Release Version: v0.9.0-shared-expense-settlements

Status: Ready for Implementation

---

## Sprint Goal

Sprint 06 completes the collaborative finance domain in ExpenseFlow.

Until Sprint 05, Groups and Trips exist as organizational workspaces but contain no financial logic. Sprint 06 introduces the full shared expense lifecycle — creation, editing, deletion, split calculation, and debt settlement — making Groups and Trips financially active for the first time.

This sprint also closes the outstanding Sprint 04 technical debt (Group React Query hooks, Group UI integration, and Group unit tests) before adding new collaborative financial features on top.

The primary objectives are:

1. Complete shared expense creation, editing, and deletion within Groups and Trips.
2. Implement all four split strategies: Equal, Exact, Percentage, and Shares.
3. Implement the settlement calculation engine using a debt-minimization algorithm.
4. Expose settlement APIs and mark-as-paid flow.
5. Build the Settlement UI inside the Group and Trip workspaces.
6. Close Sprint 04 Group frontend and testing carry-overs.

---

## Prerequisites

Completed:

- Sprint 00 — Foundation
- Sprint 01 — Authentication
- Sprint 01D — Testing & QA
- Sprint 02 — Dashboard & User Preferences
- Sprint 03 — Personal Expense Management
- Sprint 04 — Group Management
- Sprint 05 — Trip Management Workspace
- v0.7.0-dashboard-redesign (Dashboard UI Modernization)
- v0.8.0-profile-redesign (Profile UI Modernization)

---

## Read Before Implementation

Developers should review the following project documentation before starting Sprint 06:

- `MASTER_CONTEXT.md`
- `ENGINEERING_GUIDELINES.md`
- `PROJECT_STRUCTURE.md`
- `UI_STYLE_GUIDE.md`
- `TECH_STACK.md`
- `docs/architecture/ADR-001-Group-Architecture.md`
- `docs/architecture/ADR-002-Trip-Architecture.md`
- `docs/architecture/ADR-005-Shared-Expense-Settlement-Architecture.md` (to be authored alongside this sprint)

---

## Bounded Context

This sprint operates exclusively within the **Collaborative Expense** bounded context.

The following bounded contexts are **out of scope** and must not be modified:

- User / Profile Management (Sprint 07)
- Notification Center (Sprint 08)
- Analytics & Reporting (Sprint 09)
- Authentication / Security
- Personal Finance (Expenses, Income, Budgets, Savings)

---

## Sprint Scope

### Included

#### Track A — Close Sprint 04 Technical Debt (Highest Priority)

These items were left incomplete at the end of Sprint 04 and must be resolved before new collaborative financial features are layered on top.

**Group Frontend Integration**
- Implement React Query hooks for Groups:
  - `useMyGroups` — paginated active group list
  - `useGroupDetail` — full group details with members and activity
  - `useGroupMembers` — member roster with role badges
  - `useGroupActivity` — paginated activity timeline
  - `useJoinGroup` — join by room code mutation
  - `useLeaveGroup` — leave group mutation
  - `useArchiveGroup` — archive mutation (owner only)
- Implement Zod validation schemas for all group forms
- Complete `GroupDetailPage` integration:
  - Member roster tab with role badges and kick/promote/demote controls
  - Group settings tab with archive/restore toggle
  - Activity timeline tab

**Group Unit & Integration Tests**
- Unit tests for `GroupCommandService` using mocks
- Unit tests for `GroupQueryService` using mocks
- Unit tests for `MemberCommandService` using mocks
- ArchUnit architectural validation:
  - Repositories do not access other repositories
  - Controllers do not access repositories directly

---

#### Track B — Shared Expense Management

**Database Schema**
- Flyway migration `V8__shared_expenses.sql`:
  - `shared_expenses` table with columns: `id`, `group_id`, `trip_id` (nullable), `title`, `description`, `amount`, `currency`, `split_type`, `expense_status`, `paid_by_user_id`, `receipt_url`, audit fields, soft delete columns
  - `shared_expense_splits` table: `id`, `expense_id`, `user_id`, `split_amount`, `split_percentage`, `split_shares`, `is_settled`, audit fields
  - `shared_expense_participants` table: `id`, `expense_id`, `user_id`, `role` (PAYER / PARTICIPANT)
  - Indexes: `idx_shared_expenses_group`, `idx_shared_expenses_trip`, `idx_shared_expenses_payer`, `idx_splits_expense`, `idx_splits_user`

**Domain Value Objects**
- `SplitType` enum: `EQUAL`, `EXACT`, `PERCENTAGE`, `SHARES`
- `ExpenseStatus` enum: `DRAFT`, `POSTED`, `SETTLED`, `CANCELLED`
- `ParticipantRole` enum: `PAYER`, `PARTICIPANT`
- `CurrencyConsistencyPolicy` — validates all splits use the group's default currency

**Aggregate Root**
- `SharedExpenseEntity` preserving all aggregate invariants:
  - Payer must be a group member
  - Split amounts must sum exactly to total amount (within 1 cent tolerance for rounding)
  - Percentage splits must sum to exactly 100%
  - Only `DRAFT` expenses may be edited
  - Only `POSTED` expenses are included in settlement calculations
  - Soft delete only — financial records are never permanently deleted
  - Apply `@Version` for optimistic locking to prevent concurrent modification conflicts

**Posted Expense Immutability Rule**

Once an expense transitions from `DRAFT` to `POSTED`, the following fields become immutable:

- `amount` cannot change
- `participants` cannot change
- `splitStrategy` cannot change
- `paidByUserId` (payer) cannot change

Only cancellation (`POSTED` → `CANCELLED`) is permitted after posting. Any attempt to mutate a `POSTED` expense must throw an `InvalidExpenseStateException` with error code `EXPENSE_003`.

**Split Calculators (Strategy Pattern)**
- `EqualSplitCalculator` — divides total evenly, distributes rounding remainder to first participant
- `ExactSplitCalculator` — validates provided amounts sum to total
- `PercentageSplitCalculator` — validates percentages sum to 100, converts to amounts
- `SharesSplitCalculator` — computes proportional amounts from share counts
- `SplitCalculatorFactory` — resolves the correct strategy by `SplitType`

**Repositories**
- `SharedExpenseRepository` — paginated queries by group, by trip, by payer, by status
- `SharedExpenseSplitRepository` — unsettled splits by user, splits by expense

**Services (CQRS-Lite)**
- `SharedExpenseCommandService`:
  - `createExpense(request, groupId, tripId?)` — validates membership, invokes split calculator, persists, publishes `SharedExpenseCreatedEvent`
  - `updateExpense(expenseId, request)` — validates DRAFT status before allowing mutation
  - `postExpense(expenseId)` — transitions DRAFT → POSTED, locks splits for settlement
  - `deleteExpense(expenseId)` — soft delete, validates DRAFT or CANCELLED status only
  - `cancelExpense(expenseId)` — transitions POSTED → CANCELLED (owner or group admin only)
- `SharedExpenseQueryService`:
  - `getExpensesByGroup(groupId, pageable)`
  - `getExpensesByTrip(tripId, pageable)`
  - `getExpenseDetail(expenseId)`
  - `getMyExpenses(userId, groupId)`

**Domain Events**
- `SharedExpenseCreatedEvent`
- `SharedExpenseUpdatedEvent`
- `SharedExpensePostedEvent`
- `SharedExpenseCancelledEvent`
- `SharedExpenseDeletedEvent`
- `SplitCalculatedEvent`

**REST Controller**
- `SharedExpenseController` at `/api/v1/groups/{groupId}/expenses`
- `GET /` — paginated expense list for a group
- `POST /` — create a new shared expense
- `GET /{expenseId}` — expense detail with splits
- `PUT /{expenseId}` — update a DRAFT expense
- `POST /{expenseId}/post` — transition to POSTED
- `POST /{expenseId}/cancel` — cancel a POSTED expense
- `DELETE /{expenseId}` — soft delete a DRAFT expense
- Trip-scoped variant: `/api/v1/groups/{groupId}/trips/{tripId}/expenses`

**Validation**
- Group membership check before expense creation
- Trip membership check when `tripId` is provided
- Currency consistency enforcement against group default currency
- Split sum validation with cent-level tolerance

---

#### Track C — Settlement Engine

**Database Schema**
- Flyway migration `V9__settlements.sql`:
  - `settlements` table: `id`, `group_id`, `trip_id` (nullable), `from_user_id`, `to_user_id`, `amount`, `currency`, `status` (`PENDING`, `CONFIRMED`, `DISPUTED`), `settled_at`, audit fields
  - `settlement_history` table: append-only log of status transitions
  - Index: `idx_settlements_group`, `idx_settlements_trip`, `idx_settlements_from_to`

**Settlement State Machine**

Only the following status transitions are permitted. Any other transition must throw `InvalidSettlementStateException`:

```
PENDING
   │
   ├──► CONFIRMED   (payer confirms payment)
   │
   └──► DISPUTED    (receiver raises a dispute)

DISPUTED
   │
   └──► CONFIRMED   (dispute resolved manually)
```

`SettlementEntity` must apply `@Version` for optimistic locking.

**Settlement Generation — Idempotency Rule**

Settlement generation must be deterministic and idempotent.

Running `generateSettlements` multiple times without any expense changes must:
- Produce identical `(from, to, amount)` settlement pairs
- Not create duplicate settlement records

Implementation strategy: before persisting, check for an existing `PENDING` settlement between the same `(from_user_id, to_user_id)` pair within the same group/trip scope. If one exists, update its amount rather than inserting a duplicate.

**Settlement Calculation Algorithm**
- `NetBalanceCalculator` — computes net balance per user from all POSTED expense splits in a group/trip:
  - Positive balance = user is owed money
  - Negative balance = user owes money
- `DebtMinimizationSolver` — greedy algorithm to minimize the number of transactions required to settle all debts:
  - Sort creditors (positive balances) and debtors (negative balances) in descending order
  - Iteratively match largest creditor with largest debtor
  - Produces the minimum set of `(from, to, amount)` settlement pairs

**Performance Target**

Settlement generation for a group with 100 members and 1,000 POSTED expenses must complete within 1 second on a development machine. This should be validated manually before the sprint is closed.

**Services**
- `SettlementCommandService`:
  - `generateSettlements(groupId, tripId?)` — runs net balance + debt minimization, persists settlement records
  - `markAsPaid(settlementId, payerId)` — transitions PENDING → CONFIRMED, marks related splits as settled
  - `disputeSettlement(settlementId)` — transitions CONFIRMED → DISPUTED
- `SettlementQueryService`:
  - `getSettlementSummary(groupId)` — who owes whom, total amounts
  - `getSettlementsByTrip(tripId)`
  - `getMySettlements(userId, groupId)` — amounts owed by/to current user

**Domain Events**
- `SettlementsGeneratedEvent`
- `SettlementPaidEvent`
- `SettlementDisputedEvent`

**REST Controller**
- `SettlementController` at `/api/v1/groups/{groupId}/settlements`
- `GET /` — settlement summary for a group
- `POST /generate` — trigger settlement recalculation
- `GET /my` — current user's owed/owing positions
- `POST /{settlementId}/pay` — mark as paid
- `POST /{settlementId}/dispute` — raise a dispute
- Trip-scoped variant: `/api/v1/groups/{groupId}/trips/{tripId}/settlements`

---

#### Track D — Frontend Integration

**Shared Expense Hooks**
- `useGroupExpenses(groupId, page)` — paginated expense list
- `useTripExpenses(groupId, tripId, page)` — trip-scoped expense list
- `useExpenseDetail(expenseId)` — full detail with splits
- `useCreateExpense()` — mutation
- `useUpdateExpense()` — mutation
- `usePostExpense()` — mutation
- `useDeleteExpense()` — mutation

**Settlement Hooks**
- `useSettlementSummary(groupId)` — who owes whom
- `useTripSettlements(groupId, tripId)` — trip-scoped settlement list
- `useMySettlements(groupId)` — current user's positions
- `useGenerateSettlements()` — mutation
- `useMarkAsPaid()` — mutation

**Frontend Components**
- `CreateSharedExpenseDialog` — multi-step form:
  - Step 1: Title, amount, currency, payer, participants
  - Step 2: Split strategy selector (Equal / Exact / Percentage / Shares)
  - Step 3: Per-participant split inputs (rendered conditionally by strategy)
  - Step 4: Review and confirm
- `SharedExpenseCard` — compact ledger card with split type badge, payer avatar, status chip
- `SharedExpenseLedger` — paginated list inside `GroupDetailPage` Expenses tab
- `ExpenseDetailDrawer` — right-side drawer showing full expense breakdown and per-user split amounts
- `SettlementSummaryCard` — "Who owes whom" overview card with simplified debt pairs
- `SettlementListItem` — single settlement row with payer/receiver avatars, amount, and "Mark as Paid" action
- `MarkAsPaidDialog` — confirmation dialog before committing settlement
- `TripSettlementPanel` — settlement summary embedded inside Trip detail view

**Page Integration**
- Add **Expenses** tab to `GroupDetailPage` showing `SharedExpenseLedger`
- Add **Settlements** tab to `GroupDetailPage` showing `SettlementSummaryCard` + `SettlementListItem` list
- Add **Expenses** section to Trip detail view (trip-scoped subset)
- Add **Settlements** section to Trip detail view (trip-scoped subset)

---

## Excluded from Sprint 06

The following are explicitly out of scope:

- Profile editing, avatar upload, password change (Sprint 07)
- Notification Center and email delivery (Sprint 08)
- Analytics, forecasting, and report exports (Sprint 09)
- OCR receipt scanning and AI features (Future)
- Offline synchronization (Future)
- Currency conversion across different currencies within one group
- Recurring shared expenses

---

## Future Enhancements

The following items were consciously deferred and should be revisited in future sprints or versions:

- Cross-currency settlements (multi-currency group support with live exchange rates)
- Partial settlement payments (pay a portion of a debt rather than the full amount)
- Recurring shared expenses (subscription-style group bills that auto-generate on a schedule)
- Split templates (save a participant + strategy preset for reuse across expenses)
- Expense approval workflow (require group admin approval before an expense is POSTED)
- OCR receipt attachment parsing (extract amount and merchant from a photo)
- AI-suggested splits (infer participants and amounts from receipt content)

---

## Git Commit Convention

| Prefix | Scope |
|---|---|
| `feat(shared-expense):` | Shared expense domain — entities, services, controllers |
| `feat(settlement):` | Settlement engine — calculator, services, controllers |
| `feat(frontend):` | React components, hooks, page integration |
| `fix(group):` | Sprint 04 carry-over Group frontend fixes |
| `test(shared-expense):` | Unit and integration tests for expense domain |
| `test(settlement):` | Unit tests for settlement calculator |
| `test(group):` | Sprint 04 carry-over Group service tests |
| `docs(sprint-06):` | Sprint documentation |

---

## Definition of Done

A sprint is complete **only if** all of the following are verified:

- [ ] `mvn clean compile` — Backend builds with zero errors
- [ ] `npm run build` — Frontend builds with zero TypeScript errors
- [ ] `npm run test` — All frontend test suites pass
- [ ] Backend unit tests pass for all new services
- [ ] ArchUnit architecture tests pass
- [ ] Split sum validation enforced (cent-level tolerance)
- [ ] Mutating a POSTED expense throws `InvalidExpenseStateException` (verified via unit test)
- [ ] Settlement generation is idempotent — running twice without expense changes produces no duplicate records
- [ ] Debt minimization produces correct settlement pairs (verified manually with a known dataset)
- [ ] Settlement generation for 100 members / 1,000 expenses completes within 1 second
- [ ] Settlement state machine rejects invalid transitions
- [ ] Settlement mark-as-paid correctly updates split records
- [ ] `@Version` optimistic locking applied to `SharedExpenseEntity` and `SettlementEntity`
- [ ] No console errors in browser
- [ ] Responsive layout verified at 1440px, 1024px, and 375px
- [ ] Light Mode and Dark Mode verified
- [ ] No existing Sprint 01–05 features regressed
- [ ] ADR-005 authored and committed
- [ ] Git commit created per convention
- [ ] Release tagged `v0.9.0-shared-expense-settlements`

---

## Recommended Implementation Sequence

This order aligns with the dependency graph defined in [ADR-005](file:///E:/ExpenseTrack/docs/architecture/ADR-005-Shared-Expense-Settlement-Architecture.md). Each step must compile and pass existing tests before the next step begins.

| Step | Task | Track |
|---|---|---|
| 1 | Flyway `V8__shared_expenses.sql` — schema, indexes | B |
| 2 | Domain value objects — `Money`, `CurrencyCode`, `SplitAmount`, `Percentage`, `ShareCount` | B |
| 3 | Domain-specific exceptions — `MoneyValidationException`, `SplitValidationException`, etc. | B |
| 4 | `SharedExpenseEntity` aggregate root with invariant enforcement | B |
| 5 | Split calculators — `Equal`, `Exact`, `Percentage`, `Shares` + `SplitCalculatorFactory` | B |
| 6 | `SharedExpenseCommandService` + `SharedExpenseQueryService` | B |
| 7 | `SharedExpenseController` REST endpoints | B |
| 8 | Flyway `V9__settlements.sql` — schema, indexes | C |
| 9 | `NetBalanceCalculator` domain service | C |
| 10 | `DebtMinimizationSolver` domain service | C |
| 11 | `SettlementCommandService` + `SettlementQueryService` | C |
| 12 | `SettlementController` REST endpoints | C |
| 13 | Group React Query hooks + Group UI tabs (Track A carry-over) | A |
| 14 | Shared Expense React Query hooks + `CreateSharedExpenseDialog`, `SharedExpenseLedger` | D |
| 15 | Settlement React Query hooks + `SettlementSummaryCard`, `MarkAsPaidDialog` | D |
| 16 | Group and Trip page tab integration | D |
| 17 | Unit tests — calculators, aggregate invariants, value objects | B/C |
| 18 | Integration tests — repositories, controllers, Flyway migrations | B/C |
| 19 | ArchUnit architectural validation | All |
| 20 | Performance validation — settlement generation SLA | C |

---

## Version Roadmap

| Sprint | Release | Domain |
|---|---|---|
| Sprint 00–03 | v0.1.0 – v0.4.0 | Foundation, Auth, Personal Finance |
| Sprint 04 | v0.5.0 | Group Management |
| Sprint 05 | v0.6.0 | Trip Management |
| Sprint 05B | v0.7.0 | Dashboard UI Redesign |
| Sprint 05C | v0.8.0 | Profile UI Redesign |
| **Sprint 06** | **v0.9.0** | **Shared Expense Management & Settlements** |
| Sprint 07 | v1.0.0 | Profile & Account Management |
| Sprint 08 | v1.1.0 | Notifications & Productivity |
| Sprint 09 | v1.2.0 | Analytics & Reporting |
| Future | v2.0.0+ | OCR, AI, Offline Sync |
