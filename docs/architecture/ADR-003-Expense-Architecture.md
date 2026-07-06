# ADR-003: Expense Bounded Context Architecture

## 1. Status
*   **ADR Number:** ADR-003
*   **Status:** ACCEPTED
*   **Version:** 1.3
*   **Sprint:** 06
*   **Supersedes:** None
*   **Depends On:** [ADR-001 Group Architecture](file:///e:/ExpenseTrack/docs/architecture/ADR-001-Group-Architecture.md), [ADR-002 Trip Architecture](file:///e:/ExpenseTrack/docs/architecture/ADR-002-Trip-Architecture.md)

---

## 2. Context
The Expense bounded context manages shared financial transactions within collaborative groups.
It is responsible for:
*   Expense lifecycle (creating, updating, deleting) and status transitions (`DRAFT`, `POSTED`, `VOID`).
*   Expense categorization and attachment associations with storage provider logging.
*   Split calculations among group members (EQUAL, EXACT, PERCENTAGE, SHARES) using decoupled strategy calculators.
*   Tracking who paid (single payer per expense) and how much each participant owes.
*   Cross-context links to parent Groups and optional Trips.

It is **not** responsible for:
- Group member lists and role permissions (managed by Groups).
- Trip lifecycle and itineraries (managed by Trips).
- Settlements, debt resolution, or clearing balances (Sprint 07).
- Analytics (Sprint 08).

---

## 3. Aggregate Boundary

### Aggregate Root: `Expense`
The `Expense` aggregate represents a single shared financial transaction.

### Encapsulated Entities
*   **ExpenseParticipant**: Represents a user involved in the expense.
*   **ExpenseSplit**: Represents the debt distribution details (how much a user owes and why).
*   **ExpenseAttachment**: Represents receipts, PDFs, or photos associated with the expense.

### Aggregate Ownership
*   **Expense Aggregate Owns:**
    *   `Expense`
    *   `ExpenseParticipant`
    *   `ExpenseSplit`
    *   `ExpenseAttachment`
*   **Expense Aggregate References:**
    *   `Group` (`groupId` only)
    *   `Trip` (`tripId` optional, only referenced by ID)
    *   `User` (referenced by `userId` for payers, creators, participants, and splits)

---

## 4. Aggregate Invariants

An Expense aggregate must always satisfy the following constraints (Single Payer Model - Option A):
*   `Expense.paidByUserId` is explicitly recorded as the sole payer for the transaction.
*   `Sum(ExpenseSplit.owedAmount) == Expense.totalAmount`
*   All participants (`ExpenseParticipant`) and split debtors (`ExpenseSplit`) must be active members of the parent Group.
*   Split currency must equal Expense currency.
*   `POSTED` expenses are immutable except for soft deletion.
*   `VOID` expenses are read-only.

---

## 5. Attachment Policy
*   Attachments are metadata only.
*   The `Expense` aggregate never owns binary files.
*   Only URLs and metadata (filename, size, filetype) are stored in the database.
*   Binary storage systems remain external to the bounded context.

---

## 6. Concurrency Strategy
*   Every mutable aggregate and supporting entity participates in optimistic locking using `@Version`.
*   Concurrent updates that violate version checks fail and are retried by the client.

---

## 7. Transaction Boundary
*   Each command operation executes within a single transaction boundary.
*   All `Expense`, `ExpenseParticipant`, `ExpenseSplit`, and `ExpenseAttachment` changes commit atomically.
*   Cross-context operations (e.g. calling group or trip services) are never part of the same database transaction.

---

## 8. Domain Event Policy
*   Domain events (`ExpenseCreatedEvent`, `ExpenseUpdatedEvent`, `ExpenseDeletedEvent`, `ExpensePostedEvent`, `SplitCalculatedEvent`) are published only after successful aggregate persistence.
*   Events represent facts.
*   Query services never publish events.

---

## 9. Value Objects & Enums

*   **Money**: Value object containing `amount` (BigDecimal) and type-safe `CurrencyCode` enum.
*   **CurrencyCode**: `INR`, `USD`, `EUR`, `GBP`. Additional ISO-4217 currencies may be introduced in future releases. Currency conversion remains outside the Expense bounded context.
*   **SplitType**: `EQUAL`, `EXACT`, `PERCENTAGE`, `SHARES`.
*   **ExpenseCategory**: `FOOD`, `LODGING`, `TRANSPORT`, `ENTERTAINMENT`, `SHOPPING`, `OTHER`.
*   **ExpenseCategoryType**: `SYSTEM`, `CUSTOM`.
*   **ExpenseStatus**: `DRAFT`, `POSTED`, `VOID`.
*   **StorageProvider**: `LOCAL`, `S3`, `SUPABASE`, `CLOUDINARY`.

---

## 10. External References & Identifier Policy

### External References
*   `groupId`: Direct link to parent Group.
*   `tripId`: Optional link to associate an expense with a trip.
*   `paidByUserId`: Direct link to the user who paid.
*   `createdByUserId`: Direct link to the user who recorded the expense.
*   `userId`: Inside participants and splits, links to User profiles.

### Identifier Policy
*   All identifiers (`expenseId`, `expenseParticipantId`, `expenseSplitId`, `expenseAttachmentId`) are application-generated UUIDs.
