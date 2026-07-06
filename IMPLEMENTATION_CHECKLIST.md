# Sprint 04 Implementation Checklist

This document details the step-by-step implementation sequence for Sprint 04 (Group Management). All code modifications must adhere strictly to the rules defined in `ADR-001-Group-Architecture.md`.

---

## Git Commit Convention

Please format commit messages according to the following mapping:
*   `feat(group): add database schema` (Database migration scripts)
*   `feat(group): implement entities` (JPA models, Lombok configurations, Value Objects)
*   `feat(group): add repositories` (Spring Data JPA interfaces)
*   `feat(group): implement validation` (Validation layers and business rules validators)
*   `feat(group): implement group services` (GroupCommandService, GroupQueryService implementation)
*   `feat(group): implement member services` (MemberCommandService, MemberQueryService implementation)
*   `feat(group): implement access service` (`GroupAccessService` implementation)
*   `feat(group): expose REST APIs` (REST Controllers, response envelopes, & Spring Security configs)
*   `feat(frontend): add group pages` (UI components, React Query hooks, and pages)
*   `test(group): add unit tests` (Mock and unit tests)
*   `docs(group): update ADR and API specs` (Documentation updates)

---

## Standard Phase Definition of Done (DoD)

Each phase is considered complete **only if** all of the following checks are met:
*   [ ] Project builds successfully (`mvn clean compile` or `npm run build`).
*   [ ] All existing regression unit tests pass.
*   [ ] No PMD violations or Checkstyle errors in modified code.
*   [ ] ArchUnit architecture tests pass successfully.
*   [ ] Documentation updated (ADR, checklist, or API spec as relevant).
*   [ ] Git commit is created matching the Git Commit Convention.

---

## Phase 1: Lombok Builder Regression Fixes
*   [x] Add `@Builder.Default` to all pre-initialized entity fields (such as `is_deleted = 0`) across Sprints 01-03 domains.
*   [x] Run backend unit tests to verify no regressions exist.
*   *DoD Verification:*
    *   [x] Project Builds | [x] Tests Pass | [x] Commit Created

## Phase 2: Database Schema & Migration Setup
*   [x] Create Flyway migration file `backend/src/main/resources/db/migration/V6__group_management.sql`.
*   [x] Define `groups` table (columns: `id`, `name`, `description`, `currency`, `group_code`, `owner_id`, `allow_join_code`, `allow_join_link`, `archived`, audit fields).
*   [x] Define `group_members` join table with composite unique index on `(group_id, user_id)`.
*   [x] Define `group_activity` timeline table.
*   [x] Add database composite indexes:
    *   `idx_group_members_group_user` on `group_members(group_id, user_id)`
    *   `idx_group_members_group_role` on `group_members(group_id, role)`
    *   `idx_group_members_user` on `group_members(user_id)`
    *   `idx_group_activity_group_date` on `group_activity(group_id, created_at)`
*   *DoD Verification:*
    *   [x] Project Builds | [x] Tests Pass | [x] Commit Created

## Phase 3: JPA Entities, Mappers & Value Objects
*   [x] Implement `GroupCode.java` value object (with self-validation on construction).
*   [x] Implement `GroupRole.java` and `ActivityType.java` enums.
*   [x] Implement `GroupSettings.java` value object.
*   [x] Implement `GroupEntity.java`, `GroupMemberEntity.java`, and `GroupActivityEntity.java`.
*   [x] Implement MapStruct mapping conversions between entities and DTOs.
*   *DoD Verification:*
    *   [x] Project Builds | [x] Tests Pass | [x] Commit Created

## Phase 4: Spring Data Repositories
*   [x] Implement `GroupRepository.java` (including room code exist/search checks with pagination support).
*   [x] Implement `GroupMemberRepository.java` (including owner extraction).
*   [x] Implement `GroupActivityRepository.java` (including timeline pagination).
*   [x] Configure soft-delete criteria filters on repositories.
*   *DoD Verification:*
    *   [x] Project Builds | [x] Tests Pass | [x] Commit Created

## Phase 5: Validation & Exception Handling
*   [x] Implement validation subpackage classes:
    *   `GroupValidator.java`
    *   `MemberValidator.java`
    *   `OwnershipValidator.java`
    *   `PermissionValidator.java`
    *   `RoomCodeValidator.java`
*   [x] Map standard HTTP exceptions to application error code ranges (GROUP_001 to GROUP_999).
*   *DoD Verification:*
    *   [x] Project Builds | [x] Tests Pass | [x] Commit Created

## Phase 6: Core CQRS-Lite Services & Events
*   [x] Implement `GroupAccessService.java` (handles code generation with 8-character alphabet `ABCDEFGHJKLMNPQRSTUVWXYZ23456789`, code collision checking with max-5 loops, and link generation).
*   [x] Implement `GroupCommandService.java` (creation, archiving, restoring, and transactional ownership transfer).
*   [x] Implement `GroupQueryService.java` (paginated active group lists, search, details, and unified dashboard queries).
*   [x] Implement `MemberCommandService.java` (user joins, leaves, and demote/promote/kick controls).
*   [x] Implement `MemberQueryService.java` (consolidated into `GroupQueryService` read facade).
*   [x] Implement `ActivityQueryService.java` (consolidated into `GroupQueryService` read facade).
*   [x] Declare Domain Events list (`GroupCreatedEvent`, `GroupUpdatedEvent`, `GroupArchivedEvent`, `GroupRestoredEvent`, `GroupDeletedEvent`, `MemberJoinedEvent`, `MemberLeftEvent`, `MemberRemovedEvent`, `RoleChangedEvent`, `OwnershipTransferredEvent`).
*   *DoD Verification:*
    *   [x] Project Builds | [x] Tests Pass | [x] Commit Created

## Phase 7: REST API Controllers & Security Configs
*   [x] Implement prefix-guarded controller endpoints `/api/v1/groups`.
*   [x] Apply Bucket4j rate-limiter guards to access code resolver endpoints.
*   [x] Define request payload schemas and error mappings using standardized envelopes.
*   *DoD Verification:*
    *   [x] Project Builds | [x] Tests Pass | [x] Commit Created

## Phase 8: Frontend Hooks & UI Integration
*   [ ] Define frontend Zod validation schemas for forms.
*   [ ] Implement React Query hooks for group retrieval, details, and membership actions.
*   [ ] Build Group List dashboard, member manager panel, timeline ledger, and settings toggle screens.
*   *DoD Verification:*
    *   [ ] Project Builds | [ ] Tests Pass | [ ] Commit Created

## Phase 9: Testing & Architecture Verification
*   [ ] Write unit tests for Command and Query services using mocks.
*   [ ] Run ArchUnit architectural tests verifying dependencies (repositories do not access repositories, controllers do not access repositories).
*   *DoD Verification:*
    *   [ ] Project Builds | [ ] Tests Pass | [ ] Commit Created
