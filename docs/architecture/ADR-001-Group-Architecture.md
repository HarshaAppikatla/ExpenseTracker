# ADR-001: Group Management Architecture & Design Baseline

## Metadata
*   **Version:** 1.0
*   **Last Updated:** 2026-07-03
*   **Status:** **ACCEPTED & FROZEN** (Sprint 04 Baseline)
*   **Supersedes:** None

---

## 1. Context & Architectural Decisions

### 1.1 Owner Representation
*   **Decision:** Dual representation of ownership. The `groups` table holds `owner_id` as a foreign key. The `group_members` table stores a join record mapping the owner with a role of `OWNER`.
*   **Reason:** Speeds up group-level administrative authorization checks (eliminating join queries on membership tables) while keeping membership listings unified.
*   **Consequences:** Any ownership transfer must update both fields atomically within a single transactional boundary.

### 1.2 Room Code Immutability
*   **Decision:** The room code (`group_code`) is immutable after generation.
*   **Reason:** The room code acts as the shared identity (used in deep links, printed QR codes, and messages). Frequent regeneration invalidates active join links, causing user confusion.
*   **Consequences:** Access control is toggled via settings flags (`allowJoinByCode`, `allowJoinByLink`) instead of code rotation. Archiving and restoring groups are handled via explicit action endpoints (`/archive` and `/restore`) which update settings flags.

### 1.3 Service Refinement (GroupAccessService)
*   **Decision:** Introduce `GroupAccessService` instead of `InvitationService` or `RoomCodeService`.
*   **Reason:** The scope of this component is managing access control to a group. Responsibilities include generating codes/links, validating codes/links, and handling access via future mechanisms (QR codes, deep links).
*   **Consequences:** Maps the access engine directly to group settings flags.

### 1.4 Command-Query Responsibility Segregation (CQRS-Lite)
*   **Decision:** Split command services (mutations) from query services (reads).
*   **Reason:** Keeps service files small, simplifies read-only transaction tuning (`readOnly = true` optimization on JPA/Spring transactions), and simplifies future Sprint 08 timeline analytics queries.
*   **Consequences:** Services are divided as follows:
    *   **Commands:** `GroupCommandService`, `MemberCommandService`, `GroupAccessService`
    *   **Queries:** `GroupQueryService`, `MemberQueryService`, `ActivityQueryService`

### 1.5 DTO Isolation Rule
*   **Decision:** Entities are never exposed outside the service layer.
*   **Reason:** Exposing entities directly causes serialization issues, accidental lazy-loading failures, and leaks internal database models to clients. Mappers (MapStruct) must convert entities to Response DTOs.

---

## 2. Domain Value Objects & Embeddables

*   `GroupCode`: Encapsulates an 8-character uppercase alphanumeric string. Handles self-validation upon instantiation.
*   `GroupRole`: Enum representing member hierarchy (`OWNER`, `ADMIN`, `MEMBER`).
*   `ActivityType`: Enum representing allowed activity types (`GROUP_CREATED`, `GROUP_UPDATED`, `GROUP_ARCHIVED`, `GROUP_DELETED`, `MEMBER_JOINED`, `MEMBER_LEFT`, `ROLE_CHANGED`, `OWNER_TRANSFERRED`).
*   `GroupSettings`: JPA Embeddable or separate object encapsulating:
    *   `allowJoinByCode` (Boolean)
    *   `allowJoinByLink` (Boolean)
    *   `archived` (Boolean)

---

## 3. Package & Dependency Boundaries (ArchUnit Guarded)

### 3.1 Package Flow
*   `controller` ──> `service` (Thin controllers, business logic in services).
*   `service` ──> `repository` (Services coordinate repositories).
*   `controller` ──x `repository` (Strictly forbidden).
*   `repository` ──x `repository` (Repositories must never call other repositories).

### 3.2 Repository Responsibilities
*   `GroupRepository`:
    *   `findById(UUID id)`
    *   `findByGroupCode(GroupCode code)`
    *   `existsByGroupCode(GroupCode code)`
    *   `search(String query, Pageable pageable)`
*   `GroupMemberRepository`:
    *   `findMembersByGroupId(UUID groupId, Pageable pageable)`
    *   `findOwnerByGroupId(UUID groupId)`
    *   `existsByGroupIdAndUserId(UUID groupId, UUID userId)`
    *   `countActiveMembersByGroupId(UUID groupId)`
*   `GroupActivityRepository`:
    *   `findActivityByGroupId(UUID groupId, Pageable pageable)`
    *   `findRecentActivityByGroupId(UUID groupId, int limit)`
    *   `searchActivity(UUID groupId, String query, Pageable pageable)`

---

## 4. State Lifecycle Diagrams

### 4.1 Group State
```text
  [ ACTIVE ] ──(Archive)──> [ ARCHIVED ]
       │                         │
   (Restore) <───────────────────┘
       │
    (Delete)
       ▼
 [ SOFT_DELETED ]
```

### 4.2 Membership State
```text
 [ NOT_MEMBER ] ──(Join)──> [ ACTIVE ]
       ▲                       │
       │                    (Leave/Kick)
       │                       ▼
       └───────────────── [ LEFT ]
```

### 4.3 Ownership State
```text
 [ OWNER ] ──(Transfer Ownership)──> [ ADMIN ] ──(Promote Owner)──> [ OWNER ]
```

---

## 5. API Versioning & Structured Error Codes

### 5.1 Versioning
All endpoints are strictly prefix-guarded: `/api/v1/groups/...`

### 5.2 Structured Application Error Code Ranges
To future-proof error handling, error codes are reserved in ranges:

*   **GROUP_001 – GROUP_099 (Validation)**
    *   `GROUP_001`: Invalid Group Name
    *   `GROUP_002`: Invalid Currency Code
*   **GROUP_100 – GROUP_199 (Resource)**
    *   `GROUP_101`: Group Not Found
*   **GROUP_200 – GROUP_299 (Permission)**
    *   `GROUP_201`: Permission Denied
    *   `GROUP_202`: Owner Cannot Leave (must transfer ownership first)
*   **GROUP_300 – GROUP_399 (Access)**
    *   `GROUP_301`: Invalid or Disabled Room Code
    *   `GROUP_302`: Join By Link Disabled
*   **GROUP_900 – GROUP_999 (Internal Errors)**
    *   `GROUP_901`: Room Code Generation Collision Exhausted

---

## 6. Core Architectural Invariants

Every developer and pull request must preserve the following business rules:
1.  **Unique Owner:** Every group has exactly one Owner.
2.  **Owner Membership:** Every Owner is an active GroupMember.
3.  **Immutable Code:** Every group has one immutable Room Code.
4.  **No Duplicate Membership:** A User may belong to a Group only once.
5.  **Append-Only Log:** GroupActivity history is strictly append-only.
6.  **Soft-Deleted Invariant:** Soft-deleted groups cannot accept new members or changes.
7.  **Archived Restriction:** Archived groups cannot be modified except to Restore or Delete.
8.  **Audit Trail:** Membership changes must always append an Activity record synchronously.
9.  **Transactional Integrity:** All state-modifying group operations run inside an atomic transaction.

---

## 7. Future Sprint Dependency Roadmap

```text
Sprint 05 (Trips)
 └── Uses Group context (Adds Trip schedules and locations)

Sprint 06 (Shared Expenses & Split Engine)
 └── Uses Group context (Adds group_id mapping, split shares, bills)

Sprint 07 (Settlements)
 └── Uses Group context (Adds debt resolution and payment verification)

Sprint 08 (Analytics)
 └── Reads GroupActivity (Processes timeline statistics and spending metrics)

Sprint 09 (Group Budgets)
 └── Creates Group-level spending limits

Sprint 10 (Domain Events / Notifications)
 └── Listens to Group events (Dispatches push notifications and emails)

Sprint 11 (AI Insights)
 └── Analyzes GroupActivity and ledger records for recommendation engines
```
