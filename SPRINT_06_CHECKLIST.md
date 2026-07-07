# Sprint 06 — Implementation Checklist

**Release:** `v0.9.0-shared-expense-settlements`
**Branch:** `feature/sprint-06-shared-expenses`
**ADR:** [ADR-005](file:///E:/ExpenseTrack/docs/architecture/ADR-005-Shared-Expense-Settlement-Architecture.md)

---

## Current State (Pre-Sprint Audit)

### ✅ Already Exists — Shared Expense (Backend)
- `V8__expense_management.sql` — `expenses`, `expense_splits`, `expense_participants`, `expense_attachments` tables
- `V9__expense_freeze_fixes.sql` — `split_type` column
- `ExpenseEntity` — aggregate root with full lifecycle methods
- `ExpenseSplitEntity`, `ExpenseParticipantEntity`, `ExpenseAttachmentEntity`
- `Money`, `CurrencyCode`, `SplitType`, `ExpenseStatus`, `ExpenseCategory` value objects
- `EqualSplitCalculator`, `ExactSplitCalculator`, `PercentageSplitCalculator`, `SharesSplitCalculator`, `SplitCalculatorFactory`
- `ExpenseCommandService` / `ExpenseQueryService` interfaces + `impl/`
- `ExpenseController`
- `ExpenseException`, `ExpenseNotFoundException`, `InvalidExpenseStateException`, `ExpensePermissionDeniedException`

### ✅ Already Exists — Shared Expense (Frontend)
- `CreateExpenseDialog.tsx`, `ExpenseList.tsx`
- `useExpenses.ts`, `expenseService.ts`
- `ExpensesPage.tsx`

### ❌ Missing — Settlement Engine (Backend)
- `V10__settlements.sql` — settlements schema
- `SettlementEntity`, `SettlementHistoryEntity`
- `SettlementStatus` value object
- `NetBalanceCalculator` domain service
- `DebtMinimizationSolver` domain service
- `SettlementCommandService` / `SettlementQueryService`
- `SettlementController`
- Settlement domain events
- Settlement exceptions

### ❌ Missing — Settlement Engine (Frontend)
- Settlement React Query hooks
- `SettlementSummaryCard`, `SettlementListItem`, `MarkAsPaidDialog`
- Settlement tab in `GroupDetailPage`

### ❌ Missing — Group Frontend Integration (Track A carry-over)
- Group React Query hooks (`useMyGroups`, `useGroupDetail`, `useGroupMembers`, etc.)
- `GroupDetailPage` — Members, Settings, Activity tabs
- Group service unit tests

### ❌ Missing — Shared Expense Group Integration (Frontend)
- Shared expense `CreateExpenseDialog` inside `GroupDetailPage`
- `SharedExpenseLedger` — group-scoped expense tab

---

## Milestone 1 — Settlement Database & Domain
**Checkpoint:** `mvn clean compile` succeeds. V10 migration applies cleanly.

- [x] Create `V10__settlements.sql` — `settlements` + `settlement_history` tables + all indexes
- [x] Create `SettlementStatus` enum (`PENDING`, `CONFIRMED`, `DISPUTED`)
- [x] Create `SettlementEntity` aggregate root with `@Version` (extends `BaseEntity` and uses `Money` value object)
- [x] Create `SettlementHistoryEntity` (append-only)
- [x] Create `SettlementException`, `SettlementNotFoundException`, `InvalidSettlementStateException`, `SettlementPermissionDeniedException`, `NoPostedExpensesException`
- [x] Unit test: `SettlementEntity` state machine transitions
- *DoD:* `[ ]` Builds | `[ ]` Tests Pass | `[ ]` Migration Applies

---

## Milestone 2 — Settlement Engine (Domain Services)
**Checkpoint:** Settlement calculation verified against known test scenarios.

- [x] Create `NetBalanceCalculator` — net balance per user from POSTED splits (uses `Money` and simplified `SplitRecord`)
- [x] Create `DebtMinimizationSolver` — greedy debt minimization graph (uses `Money` and handles currency checks)
- [x] Unit test: `NetBalanceCalculator` — known payer/participant dataset
- [x] Unit test: `DebtMinimizationSolver` — verify minimum transaction count
- [x] Unit test: Idempotency — same input produces identical pairs
- *DoD:* `[ ]` Builds | `[ ]` Tests Pass

---

## Milestone 3 — Settlement Application Services & API
**Checkpoint:** Full settlement lifecycle accessible via REST. Integration tests pass.

- [ ] Create `SettlementCommandService` interface + impl:
  - `generateSettlements(groupId, tripId?)` — idempotent upsert
  - `markAsPaid(settlementId, currentUserId)` — PENDING → CONFIRMED
  - `disputeSettlement(settlementId, currentUserId)` — PENDING → DISPUTED
  - `resolveSettlement(settlementId, currentUserId)` — DISPUTED → CONFIRMED
- [ ] Create `SettlementQueryService` interface + impl:
  - `getSettlementSummary(groupId)` — who owes whom
  - `getSettlementsByTrip(tripId)`
  - `getMySettlements(userId, groupId)`
- [ ] Create `SettlementRepository` (one per aggregate root — no child repos)
- [ ] Create `SettlementController` at `/api/v1/groups/{groupId}/settlements`
- [ ] Create settlement DTOs and MapStruct mappers
- [ ] Register settlement exceptions in `GlobalExceptionHandler`
- [ ] Integration test: `SettlementController` — generate, pay, dispute, resolve
- [ ] Integration test: Idempotency — `POST /generate` twice = no duplicates
- *DoD:* `[ ]` Builds | `[ ]` Tests Pass | `[ ]` Commit Created

---

## Milestone 4 — Group Frontend Integration (Track A Carry-Over)
**Checkpoint:** `GroupDetailPage` fully functional with members, settings, and activity tabs.

- [ ] Implement React Query hooks:
  - `useMyGroups`, `useGroupDetail`, `useGroupMembers`, `useGroupActivity`
  - `useJoinGroup`, `useLeaveGroup`, `useArchiveGroup`
- [ ] Implement Zod validation schemas for group forms
- [ ] Complete `GroupDetailPage` — Members tab (roles, kick/promote/demote)
- [ ] Complete `GroupDetailPage` — Settings tab (archive/restore)
- [ ] Complete `GroupDetailPage` — Activity tab
- [ ] Backend unit tests: `GroupCommandService`, `GroupQueryService`, `MemberCommandService`
- [ ] ArchUnit: no controller → repository, no repository → repository
- *DoD:* `[ ]` Builds | `[ ]` Tests Pass | `[ ]` Commit Created

---

## Milestone 5 — Shared Expense Group Integration (Frontend)
**Checkpoint:** End-to-end shared expense creation and viewing within a Group.

- [ ] Add `useGroupExpenses`, `useTripExpenses`, `useExpenseDetail` hooks (group-scoped)
- [ ] Add `useCreateExpense`, `usePostExpense`, `useDeleteExpense` mutations
- [ ] Add **Expenses** tab to `GroupDetailPage` with `SharedExpenseLedger`
- [ ] Wire `CreateExpenseDialog` into group context (payer dropdown = group members)
- [ ] Add `ExpenseDetailDrawer` — per-user split breakdown
- [ ] Add Expenses section to Trip detail view (trip-scoped)
- *DoD:* `[ ]` Builds | `[ ]` Tests Pass | `[ ]` Commit Created

---

## Milestone 6 — Settlement Frontend
**Checkpoint:** Full settlement flow usable end-to-end in the browser.

- [ ] Add `useSettlementSummary`, `useMySettlements`, `useTripSettlements` hooks
- [ ] Add `useGenerateSettlements`, `useMarkAsPaid` mutations
- [ ] Build `SettlementSummaryCard` — "who owes whom" overview
- [ ] Build `SettlementListItem` — debt row with payer/receiver avatars + action
- [ ] Build `MarkAsPaidDialog` — confirmation before committing
- [ ] Add **Settlements** tab to `GroupDetailPage`
- [ ] Add Settlements panel to Trip detail view
- *DoD:* `[ ]` Builds | `[ ]` Tests Pass | `[ ]` Commit Created

---

## Milestone 7 — Quality Gate
**Checkpoint:** Release candidate. All DoD conditions met.

- [ ] ArchUnit: settlement does not access expense repository directly
- [ ] Performance: settlement generation (100 members, 1,000 expenses) < 1 second
- [ ] Responsive layout verified at 1440px, 1024px, 375px
- [ ] Light Mode + Dark Mode verified
- [ ] No console errors
- [ ] No existing Sprint 01–05 regressions (`npm run test` all green)
- [ ] `mvn clean compile` — zero errors
- [ ] `npm run build` — zero TypeScript errors
- [ ] Update `CHANGELOG.md` with v0.9.0 entry
- [ ] Update `SPRINT_06.md` status to Completed
- [ ] Git commit + tag `v0.9.0-shared-expense-settlements`

---

## Summary

| Milestone | Domain | Status |
|---|---|---|
| 1 — Settlement DB & Domain | Backend | ⬜ Not started |
| 2 — Settlement Engine | Backend | ⬜ Not started |
| 3 — Settlement Services & API | Backend | ⬜ Not started |
| 4 — Group Frontend (Track A) | Frontend | ⬜ Not started |
| 5 — Shared Expense Group UI | Frontend | ⬜ Not started |
| 6 — Settlement Frontend | Frontend | ⬜ Not started |
| 7 — Quality Gate | All | ⬜ Not started |
