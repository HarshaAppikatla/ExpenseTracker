Sprint 04 — Group Management

Version: 1.0

Estimated Duration: 8–10 Days

Release Version: v0.5.0-group-management

Status: Ready for Implementation

Sprint Goal

Sprint 04 introduces the first collaborative capabilities in ExpenseFlow.

Until Sprint 03, every financial record belongs to a single user. Sprint 04 introduces Groups, allowing multiple users to collaborate within a shared workspace that will later support shared expenses, trip management, settlements, budgets, and analytics.

This sprint establishes the collaboration layer only.

No shared financial transactions are implemented during this sprint.

The primary objectives are:

Create collaborative groups.
Manage group members.
Join groups using secure room codes.
Share invite links.
Manage roles and permissions.
Track group activity.
Build a scalable foundation for future collaborative financial features.
Prerequisites

Completed:

Sprint 00 — Foundation
Sprint 01 — Authentication
Sprint 01D — Testing & QA
Sprint 02 — Dashboard & User Preferences
Sprint 03 — Personal Expense Management
Read Before Implementation

Developers should review the following project documentation before starting Sprint 04:

MASTER_CONTEXT.md
ENGINEERING_GUIDELINES.md
PROJECT_STRUCTURE.md
DESIGN_SYSTEM.md
UI_STYLE_GUIDE.md
CODING_STANDARDS.md
TECH_STACK.md
Sprint Scope
Included
Group Lifecycle
Create Group
Edit Group
Archive Group
Restore Group
Delete Group (Soft Delete)
Group Avatar
Group Description
Default Group Currency
Member Management
Join Group using Room Code
Join Group using Share Link
Leave Group
Remove Member
Transfer Ownership
Promote Member to Admin
Demote Admin to Member
View Member List
Group Security
Role-based permissions
Membership validation
Ownership validation
Resource isolation
Group visibility rules
Group Dashboard
Group information
Member count
Member list
Recent activity
Group statistics
Join code display
Share link generation
Activity Timeline

Track immutable group events:

Group Created
Group Updated
Member Joined
Member Left
Member Removed
Role Changed
Ownership Transferred
Group Archived
Group Restored
Search
Search Groups
Search Members
Not Included

The following features belong to future sprints and must not be implemented here:

Shared Expenses
Expense Splitting
Settlements
Trip Workspace
Shared Budgets
Reports
Analytics
AI Features
Receipt OCR
Notification Center
Chat
Comments
File Sharing
Design Principles

Sprint 04 follows seven architectural principles.

1. Collaboration Before Transactions

Groups are collaboration containers.

They do not store financial records.

Future sprints will attach:

Shared Expenses
Trips
Settlements
Shared Budgets

to existing groups.

2. Code-First Joining

Every group automatically receives a unique room code during creation.

Users may join using:

Room Code
Share Link

No email invitation workflow exists.

This greatly simplifies onboarding.

Example:

Goa Trip

↓

Room Code

X7KD4Q2M

↓

Share Link

https://expenseflow.app/join/X7KD4Q2M
3. Domain Isolation

The Group module owns all collaboration rules.

Other domains must never manipulate group data directly.

Authentication
        │
        ▼
Group Module
        │
        ▼
Members
        │
        ▼
Activity

Future modules communicate through services and domain events.

4. Ownership First

Every group always has exactly one Owner.

Rules:

Owner cannot leave.
Owner cannot be removed.
Ownership must be transferred before leaving.
Group deletion requires Owner privileges.

This guarantees every group always has administrative ownership.

5. Immutable Activity History

Group activities are append-only.

Examples:

Member Joined

↓

Activity Created

↓

Stored Forever

Activities are never modified or deleted.

They provide a permanent audit trail.

6. Event-Driven Architecture

Every significant business action publishes a domain event.

Example:

Member Joined

↓

MemberJoinedEvent

↓

Activity Listener

↓

Activity Created

↓

Future Notification Listener

No direct service-to-service dependencies.

7. Security by Membership

Every request must validate:

Authenticated User

↓

Group Exists

↓

User is Member?

↓

Permission Check

↓

Execute Request

This prevents unauthorized access and ID enumeration.

High-Level Architecture
Authentication
        │
        ▼
──────────────────────────────────
          Group Management
──────────────────────────────────

Group Engine

Member Engine

Permission Engine

Room Code Engine

Activity Engine

──────────────────────────────────

Domain Events

Audit

Validation

Security
Domain-Driven Package Structure
com.expenseflow

group
│
├── controller
├── dto
├── entity
├── mapper
├── repository
├── service
├── validation
├── event
├── listener
├── util
└── exception

Every package owns its own business logic.

No other module accesses repositories directly.

Architecture Rules

All Sprint 04 code must follow these standards:

Constructor Injection only.
DTOs must be immutable Java Records.
UUID primary keys.
MapStruct for entity mapping.
Hibernate soft deletes.
Optimistic locking using @Version.
No field injection.
No entity exposure through REST controllers.
Domain events for cross-module communication.
Bean Validation for request validation.
Ownership enforced through authenticated user context.
Service layer contains all business logic.
Controllers remain thin.
Repositories are persistence-only.
High-Level User Flow
Creating a Group
User

↓

Create Group

↓

Validate Request

↓

Generate Unique Room Code

↓

Save Group

↓

Create Owner Membership

↓

Publish GroupCreatedEvent

↓

Log Activity

↓

Return Group Details
Joining a Group
User

↓

Enter Room Code
or
Open Share Link

↓

Validate Room Code

↓

Group Exists?

↓

Already Member?

↓

No

↓

Create Membership

↓

Publish MemberJoinedEvent

↓

Log Activity

↓

Return Success
Leaving a Group
Member

↓

Leave Group

↓

Owner?

↓

Yes

↓

Reject Request

↓

Transfer Ownership First

↓

No

↓

Remove Membership

↓

Publish MemberLeftEvent

↓

Log Activity



# Part 2 — Database Design & Group Engine

---

# Database Migration

Create a new Flyway migration.

```text
V6__group_management.sql
```

Sprint 04 introduces the collaboration layer while maintaining full backward compatibility with Sprints 01–03.

No existing tables should be modified except where a future foreign key is intentionally planned.

All new tables follow existing project conventions:

* UUID Primary Keys
* Audit Fields
* Soft Deletes
* Optimistic Locking
* Constructor-based entities
* Hibernate Filters
* Flyway Migration

---

# Database Overview

Sprint 04 introduces three new business tables.

```text
groups

group_members

group_activity
```

No invitation tables are required.

The room code itself acts as the invitation mechanism.

---

# Entity Relationship

```text
                users
                  │
                  │ 1
                  │
                  ▼
             group_members
             ▲          │
             │          │
             │ N        │ N
             │          ▼
           groups ─────► group_activity
```

Relationship Summary

* One User can belong to many Groups.
* One Group contains many Members.
* One Group contains many Activity records.
* One User may own many Groups.

---

# Table — groups

Purpose

Represents a collaborative workspace.

A group contains people.

Future sprints will attach:

* Trips
* Shared Expenses
* Settlements
* Shared Budgets

to the Group.

---

## Fields

| Field              | Type         | Description          |
| ------------------ | ------------ | -------------------- |
| id                 | UUID         | Primary Key          |
| owner_id           | UUID FK      | Current Owner        |
| name               | VARCHAR(100) | Group Name           |
| description        | VARCHAR(300) | Optional Description |
| avatar_url         | VARCHAR(500) | Group Image          |
| currency_code      | VARCHAR(10)  | Default Currency     |
| group_code         | VARCHAR(8)   | Unique Join Code     |
| allow_join_by_code | BOOLEAN      | Enable Manual Join   |
| allow_join_by_link | BOOLEAN      | Enable Share Link    |
| active             | BOOLEAN      | Active Flag          |
| version            | BIGINT       | Optimistic Lock      |
| created_at         | TIMESTAMP    | Audit                |
| updated_at         | TIMESTAMP    | Audit                |
| deleted_at         | TIMESTAMP    | Soft Delete          |

---

## Constraints

Every group must have

* Owner
* Name
* Currency
* Group Code

Rules

```text
group_code UNIQUE

owner_id NOT NULL

name NOT NULL

currency_code NOT NULL
```

---

## Unique Constraints

```text
UNIQUE(group_code)
```

No two groups may ever share the same room code.

---

## Indexes

```text
(owner_id)

(group_code)

(active)

(is_deleted)
```

---

# Group Code Design

Every group receives a unique code during creation.

Example

```text
X7KD4Q2M

PQM84ZXK

W9RT5AHL

KT7MZQ8P
```

---

## Character Set

Avoid confusing characters.

Allowed characters

```text
ABCDEFGHJKLMNPQRSTUVWXYZ

23456789
```

Excluded

```text
O

0

I

1
```

Users should never confuse

```text
O

0
```

or

```text
I

1
```

---

## Code Length

Eight characters.

```text
XXXXXXXX
```

Possible combinations

```text
32⁸

≈ 1 Trillion+
```

Collision probability is practically zero.

---

## Generation Algorithm

```text
Generate Random Code

↓

Exists?

↓

YES

↓

Generate Again

↓

NO

↓

Save
```

The database UNIQUE constraint guarantees correctness even under concurrent requests.

---

# Share Link

Every group automatically exposes a share link.

Example

```text
Room Code

X7KD4Q2M

↓

Share Link

https://expenseflow.app/join/X7KD4Q2M
```

The backend does **not** store the URL.

It is generated dynamically.

Formula

```text
BASE_URL

+

/join/

+

groupCode
```

---

# Table — group_members

Purpose

Stores group membership.

---

## Fields

| Field      | Type      |
| ---------- | --------- |
| id         | UUID      |
| group_id   | UUID FK   |
| user_id    | UUID FK   |
| role       | ENUM      |
| status     | ENUM      |
| joined_at  | TIMESTAMP |
| version    | BIGINT    |
| created_at | TIMESTAMP |
| updated_at | TIMESTAMP |
| deleted_at | TIMESTAMP |

---

## Roles

```text
OWNER

ADMIN

MEMBER
```

---

## Status

```text
ACTIVE

LEFT

REMOVED
```

Future-proof for bans if ever required.

---

## Constraints

```text
UNIQUE

group_id

user_id
```

A user may only appear once in a group.

---

## Indexes

```text
(group_id)

(user_id)

(role)

(status)
```

---

# Membership Rules

Owner

```text
Exactly One
```

Admin

```text
Zero or More
```

Member

```text
Zero or More
```

---

# Ownership Rule

Whenever ownership changes

```text
Old Owner

↓

ADMIN

↓

New Owner

↓

OWNER
```

No group can exist without an owner.

---

# Table — group_activity

Purpose

Immutable audit log.

Every important action creates one record.

Never updated.

Never deleted.

---

## Fields

| Field         | Type         |
| ------------- | ------------ |
| id            | UUID         |
| group_id      | UUID         |
| actor_user_id | UUID         |
| activity_type | ENUM         |
| message       | VARCHAR(255) |
| metadata      | JSON         |
| created_at    | TIMESTAMP    |

---

## Activity Types

```text
GROUP_CREATED

GROUP_UPDATED

GROUP_ARCHIVED

GROUP_RESTORED

GROUP_DELETED

MEMBER_JOINED

MEMBER_LEFT

MEMBER_REMOVED

ROLE_CHANGED

OWNER_TRANSFERRED
```

---

## Examples

```text
Harsha created the group.
```

```text
Rahul joined the group.
```

```text
Sneha became an Admin.
```

```text
Ownership transferred to Akhil.
```

---

## Indexes

```text
(group_id)

(created_at)

(activity_type)
```

---

# Soft Delete Strategy

Groups

```text
Deleted

↓

Hidden

↓

Recoverable
```

Members

```text
Removed

↓

Soft Delete
```

Activities

```text
Never Deleted
```

Activity history is permanent.

---

# Optimistic Locking

Editable entities use

```java
@Version
```

Applied to

* Group
* GroupMember

Not required for

* Activity

because activity records are immutable.

---

# Audit Strategy

All editable entities extend

```text
BaseEntity
```

Providing

```text
createdBy

createdAt

updatedBy

updatedAt

deletedBy

deletedAt
```

---

# Normalization Rules

No duplicated member information.

Store only

```text
user_id
```

Never duplicate

* Name
* Email
* Avatar

Those always come from the User/Profile module.

---

# Future Compatibility

This schema intentionally prepares for future modules without requiring redesign.

### Sprint 05 — Trip Workspace

```text
groups

↓

trips
```

---

### Sprint 06 — Expense Split Engine

```text
groups

↓

shared_expenses
```

---

### Sprint 07 — Settlement Engine

```text
group_members

↓

settlements
```

---

### Sprint 08 — Reports

```text
group_activity

↓

analytics
```

---

### Sprint 09 — Budgeting

```text
groups

↓

group_budgets
```

No table introduced in Sprint 04 should require structural changes to support these future features.

---

# Core Database Guarantees

Sprint 04 guarantees:

* Every group has exactly one owner.
* Every group has a globally unique room code.
* A user cannot join the same group twice.
* Group membership is fully normalized.
* Activity history is immutable.
* Soft deletes preserve historical integrity.
* Future collaboration features can attach to the existing schema without redesign.

# Part 3 — Backend Architecture & Domain Implementation

---

# Backend Philosophy

Sprint 04 introduces the **Group Management bounded context**.

This module owns all collaboration logic.

Future modules such as:

* Trip Workspace
* Expense Split Engine
* Settlement Engine
* Group Reports
* Group Budgets

must communicate with the Group module through its public services and domain events.

No future module may directly modify Group entities.

---

# Package Structure

```text
com.expenseflow

group
│
├── controller
├── dto
│   ├── request
│   └── response
├── entity
├── repository
├── mapper
├── service
├── validation
├── event
├── listener
├── exception
└── util
```

---

# Entities

Sprint 04 introduces three primary entities.

```text
GroupEntity

GroupMemberEntity

GroupActivityEntity
```

---

# GroupEntity

Represents a collaborative workspace.

Responsibilities

* Store group metadata
* Store unique room code
* Store owner
* Store default currency
* Store group settings
* Support soft delete
* Support optimistic locking

Example Fields

```java
UUID id

UUID ownerId

String name

String description

String avatarUrl

String currencyCode

String groupCode

Boolean allowJoinByCode

Boolean allowJoinByLink

Boolean active

@Version

Audit Fields
```

Business Rules

* Owner required
* Name required
* Room Code immutable
* Currency required

---

# GroupMemberEntity

Represents one user inside one group.

Fields

```java
UUID id

UUID groupId

UUID userId

GroupRole role

MemberStatus status

LocalDateTime joinedAt

@Version

Audit Fields
```

Responsibilities

* Membership
* Role
* Join date
* Member status

---

# GroupActivityEntity

Immutable audit log.

Fields

```java
UUID id

UUID groupId

UUID actorUserId

ActivityType activityType

String message

String metadata

LocalDateTime createdAt
```

Never updated.

Never deleted.

---

# DTO Design

All DTOs use immutable Java Records.

No mutable DTOs.

No Lombok Data classes.

---

## Request DTOs

```text
CreateGroupRequest

UpdateGroupRequest

JoinGroupRequest

LeaveGroupRequest

TransferOwnershipRequest

UpdateMemberRoleRequest
```

Example

```java
public record CreateGroupRequest(

        String name,

        String description,

        String currencyCode,

        String avatarUrl

){}
```

---

## Response DTOs

```text
GroupResponse

GroupSummaryResponse

GroupDetailResponse

MemberResponse

ActivityResponse

GroupStatisticsResponse
```

---

# Repository Layer

Repositories only perform persistence.

No business logic.

---

## GroupRepository

Responsibilities

* CRUD
* Find by owner
* Find by room code
* Search groups
* Pagination

Methods

```java
findByGroupCode()

existsByGroupCode()

findByOwner()

search()

findActiveGroups()
```

---

## GroupMemberRepository

Responsibilities

* Membership
* Role lookup
* Member count

Methods

```java
findMembers()

findAdmins()

findOwner()

existsMembership()

countMembers()
```

---

## ActivityRepository

Responsibilities

* Save activity
* Timeline
* Pagination

Methods

```java
findRecent()

findTimeline()

findByGroup()
```

---

# Mapper Layer

Use MapStruct.

Never map inside controllers.

```text
GroupMapper

↓

Entity

↓

DTO
```

Required Mappers

```text
GroupMapper

MemberMapper

ActivityMapper
```

---

# Service Layer

The service layer owns all business rules.

Controllers remain thin.

---

## GroupService

Responsibilities

* Create Group
* Update Group
* Archive Group
* Restore Group
* Delete Group
* Generate Room Code
* Validate Ownership
* Search Groups

---

### Create Group Workflow

```text
Request

↓

Validate

↓

Generate Room Code

↓

Save Group

↓

Create Owner Membership

↓

Publish Event

↓

Log Activity

↓

Return DTO
```

---

### Update Group

Allowed

Owner

Admin

Not Allowed

Member

---

### Delete Group

Owner only.

Soft delete.

Never remove database rows.

---

### Search Groups

Supports

* Name
* Currency
* Member Count

Paginated.

---

# MemberService

Responsibilities

* Join Group
* Leave Group
* Remove Member
* Transfer Ownership
* Promote Admin
* Demote Admin

---

## Join Group Workflow

```text
User

↓

Enter Room Code

↓

Group Exists?

↓

Already Member?

↓

No

↓

Create Membership

↓

Activity

↓

Publish Event

↓

Return Success
```

Validation

* Group active
* Join by code enabled
* User not already a member

---

## Leave Group

Validation

```text
Owner?

↓

YES

↓

Reject

↓

Transfer Ownership

↓

NO

↓

Leave Group
```

---

## Remove Member

Hierarchy

```text
Owner

↓

Admin

↓

Member
```

Rules

Owner

✔ Remove Admin

✔ Remove Member

Admin

✔ Remove Member

❌ Remove Owner

Member

❌ Remove Anyone

---

## Transfer Ownership

Validation

* New owner exists
* Active member
* Not current owner

Workflow

```text
Old Owner

↓

ADMIN

↓

New Owner

↓

OWNER

↓

Activity

↓

Event
```

---

# ActivityService

Responsibilities

* Create activity
* Timeline
* Pagination
* Recent activity

No update APIs.

No delete APIs.

Activity is immutable.

---

# GroupAccessService

Dedicated utility.

Responsibilities

* Generate room codes
* Collision detection
* Validation

Algorithm

```text
Generate Random

↓

Exists?

↓

YES

↓

Retry

↓

NO

↓

Return
```

Character Set

```text
ABCDEFGHJKLMNPQRSTUVWXYZ

23456789
```

Length

```text
8
```

---

# Domain Events

Every important action publishes one immutable event.

Events

```text
GroupCreatedEvent

GroupUpdatedEvent

GroupArchivedEvent

GroupRestoredEvent

GroupDeletedEvent

MemberJoinedEvent

MemberLeftEvent

MemberRemovedEvent

OwnershipTransferredEvent

RoleChangedEvent
```

Events contain

```text
Group ID

User ID

Timestamp

Primitive values only
```

Never expose entities.

---

# Event Listeners

Sprint 04 uses listeners only for internal side effects.

Example

```text
MemberJoinedEvent

↓

Activity Listener

↓

Create Activity
```

Future

```text
MemberJoinedEvent

↓

Notification Listener

↓

Sprint 10
```

This allows new functionality without modifying existing services.

---

# Validation Rules

## Create Group

* Name required
* Name ≤ 100 characters
* Currency required
* Description ≤ 300 characters

---

## Join Group

* Room code required
* Room code valid
* Group active
* User not already a member

---

## Leave Group

* User must be active member
* Owner cannot leave
* Archived groups cannot accept membership changes

---

## Update Role

* Cannot assign multiple owners
* Owner cannot demote themselves
* Admin cannot promote another admin to owner
* Members cannot update roles

---

# Transaction Management

Every write operation uses a single transaction.

```text
Create Group

↓

@Transactional
```

```text
Join Group

↓

@Transactional
```

```text
Transfer Ownership

↓

@Transactional
```

Activity creation should participate in the same transaction to ensure consistency. If the transaction rolls back, no activity record should remain.

---

# Exception Hierarchy

Create domain-specific exceptions.

```text
GroupNotFoundException

GroupAlreadyArchivedException

InvalidRoomCodeException

AlreadyGroupMemberException

NotGroupMemberException

OwnerCannotLeaveGroupException

OwnershipTransferException

InsufficientGroupPermissionException

DuplicateRoomCodeException
```

Global exception handling remains inside the existing `core.exception` package.

---

# REST API Contract

## Groups

```http
GET    /api/v1/groups
GET    /api/v1/groups/{id}
POST   /api/v1/groups
PATCH  /api/v1/groups/{id}
DELETE /api/v1/groups/{id}
PATCH  /api/v1/groups/{id}/archive
PATCH  /api/v1/groups/{id}/restore
```

---

## Join Group

```http
POST /api/v1/groups/join
```

Request

```json
{
  "groupCode": "X7KD4Q2M"
}
```

---

## Members

```http
GET    /api/v1/groups/{id}/members

POST   /api/v1/groups/{id}/leave

DELETE /api/v1/groups/{id}/members/{memberId}

PATCH  /api/v1/groups/{id}/members/{memberId}/role

POST   /api/v1/groups/{id}/transfer-ownership
```

---

## Activity

```http
GET /api/v1/groups/{id}/activity
```

Supports

* Pagination
* Sort
* Date filtering (optional)

---

# Architectural Rules

* Controllers never access repositories directly.
* Services never expose JPA entities.
* DTOs must be immutable Java Records.
* MapStruct performs all entity mappings.
* Constructor injection only.
* Every endpoint requires authentication.
* Ownership and membership must always be derived from the authenticated user, never from client-supplied user IDs.
* Room codes are generated only by the backend and are immutable after group creation.
* All write operations use `@Transactional`.
* Cross-module communication must occur through domain events.

# Part 4 — Frontend Architecture & User Experience

---

# Frontend Philosophy

Sprint 04 extends ExpenseFlow from a personal finance application into a collaborative workspace.

The Group module should feel like a natural extension of Sprint 03.

The UI should emphasize:

* Simplicity
* Collaboration
* Accessibility
* Performance
* Mobile-first responsiveness

Users should be able to create or join a group within **30 seconds**.

---

# Technology Stack

Continue using the existing frontend stack.

* React 19
* TypeScript
* Vite
* Material UI (M3)
* React Router
* TanStack Query
* React Hook Form
* Zod
* Axios
* Framer Motion (existing animations only)

No additional UI frameworks.

---

# Route Structure

Introduce the following protected routes.

```text
/groups

/groups/create

/groups/:groupId

/groups/:groupId/edit

/groups/:groupId/members

/groups/:groupId/activity

/join

/join/:groupCode
```

All routes require authentication.

If a user opens a shared link while not logged in:

```text
Share Link

↓

Login

↓

Automatically Continue

↓

Join Group
```

---

# Sidebar Navigation

Add a new section after **Personal Finance**.

```text
Dashboard

Expenses

Income

Categories

Receipts

-------------------------

Groups

-------------------------

Settings
```

Future sprints will nest under Groups.

```text
Groups

Trips

Shared Expenses

Settlements
```

No redesign later.

---

# Dashboard Enhancements

Extend the Sprint 03 dashboard.

Add a new widget.

```text
──────────────────────────────

Groups

3 Active Groups

[ View ]

──────────────────────────────
```

Recent Groups

```text
Goa Trip

5 Members

Last Active

Yesterday
```

Quick Actions

```text
+ Create Group

Join Group
```

---

# Groups Page

Route

```text
/groups
```

Purpose

Manage all collaborative groups.

---

## Layout

```text
-------------------------------------------------

Groups

Create and manage shared workspaces.

-------------------------------------------------

Summary Cards

My Groups

Owned

Joined

Archived

-------------------------------------------------

Search

+ Create Group

Join Group

-------------------------------------------------

Groups Grid

-------------------------------------------------
```

---

## Group Card

Each group card contains

```text
Avatar

Group Name

Description

Member Count

Currency

Role Badge

Recent Activity

Actions
```

Example

```text
━━━━━━━━━━━━━━━━━━━━━━

🏖 Goa Trip

6 Members

INR

OWNER

Last Active

2 hours ago

━━━━━━━━━━━━━━━━━━━━━━
```

---

# Create Group Page

Route

```text
/groups/create
```

Fields

* Group Name
* Description
* Avatar
* Default Currency

No room code field.

The backend generates it automatically.

---

After successful creation

Display

```text
━━━━━━━━━━━━━━━━━━━━━━

Group Created Successfully

Room Code

X7KD4Q2M

[ Copy Code ]

[ Share Link ]

Go to Group

━━━━━━━━━━━━━━━━━━━━━━
```

---

# Join Group Page

Route

```text
/join
```

Purpose

Allow users to join using a room code.

Layout

```text
━━━━━━━━━━━━━━━━━━━━━━

Join Group

Enter Room Code

______________

[ Join ]

━━━━━━━━━━━━━━━━━━━━━━
```

Validation

* Required
* Exactly 8 characters
* Uppercase automatically
* Remove spaces

---

# Join by Share Link

Route

```text
/join/:groupCode
```

Workflow

```text
Open Link

↓

Validate Group Code

↓

Logged In?

↓

YES

↓

Join Group

↓

Redirect

↓

Group Dashboard
```

If not authenticated

```text
Open Link

↓

Login

↓

Continue Join

↓

Dashboard
```

---

# Group Details Page

Route

```text
/groups/:groupId
```

Purpose

Single source of truth for a group.

---

## Header

```text
Avatar

Group Name

Description

Role

Members

Currency
```

---

## Actions

```text
Copy Room Code

Share Link

Manage Members

Edit Group

Archive
```

---

## Room Code Card

```text
━━━━━━━━━━━━━━━━━━━━━━

Room Code

X7KD4Q2M

[ Copy ]

[ Share ]

━━━━━━━━━━━━━━━━━━━━━━
```

This should always be visible to Owners and Admins.

Members can view it only if

```text
allowJoinByCode == true
```

---

# Members Page

Route

```text
/groups/:groupId/members
```

Display

```text
Owner

Admin

Members
```

Each card

```text
Avatar

Name

Role

Joined Date

Status

Menu
```

Menu

Owner

```text
Promote

Demote

Remove

Transfer Ownership
```

Admin

```text
Remove Member
```

Member

No actions.

---

# Activity Timeline

Route

```text
/groups/:groupId/activity
```

Timeline

```text
Today

Harsha created the group.

----------------

Yesterday

Rahul joined.

----------------

2 Days Ago

Sneha promoted to Admin.
```

Infinite Scroll

Newest first.

---

# Search Experience

Search Bar

Supports

* Group Name
* Member Name

Debounced

300ms

---

# Empty States

## Groups

```text
👥

No Groups Yet

Create your first group

[ Create Group ]
```

---

## Members

```text
No Members
```

---

## Activity

```text
No Activity Yet
```

---

# Loading States

Every page provides

* Skeleton Cards
* Skeleton Lists
* Skeleton Tables

Never full-page spinners.

---

# Error States

Examples

```text
Invalid Room Code
```

```text
Group Not Found
```

```text
Already Joined
```

```text
Owner Cannot Leave Group
```

Display using Material Snackbar.

---

# Shared Components

Create reusable components.

```text
GroupCard

GroupAvatar

MemberCard

RoleChip

MemberList

RoomCodeCard

CopyButton

ShareButton

ActivityTimeline

ActivityCard

GroupStatisticsCard

EmptyState

SkeletonGroupCard

ConfirmationDialog
```

---

# TanStack Query Modules

Queries

```text
useGroups()

useGroup()

useMembers()

useActivity()

useSearchGroups()
```

Mutations

```text
useCreateGroup()

useJoinGroup()

useLeaveGroup()

useUpdateGroup()

useTransferOwnership()

useUpdateRole()

useRemoveMember()
```

Invalidate

```text
groups

group

members

activity
```

after successful mutations.

---

# Forms

React Hook Form

*

Zod

Validation

Create Group

```text
Name Required

Name ≤100

Description ≤300

Currency Required
```

Join Group

```text
Room Code Required

Exactly 8 Characters
```

---

# Responsive Design

Desktop

* Grid Layout
* Sidebar
* Tables

Tablet

* Two-column cards

Mobile

* Single-column cards
* Bottom sheets
* Floating Action Button

```text
+

Create Group
```

---

# Accessibility

Every page must support

* Keyboard navigation
* Screen readers
* Focus management
* WCAG AA
* High contrast

---

# Performance Requirements

Groups list

<200ms

Member list

<200ms

Join Group

<300ms

Search

Debounced

Dashboard widget

Cached

Lazy-load all Group routes.

---

# UI/UX Rules

* Consistent spacing
* Material Design 3
* Maximum animation duration **200ms**
* Confirmation dialog before destructive actions
* Copy Room Code with one click
* Native Share API when supported
* Clipboard fallback when Share API is unavailable
* Success snackbars for all completed actions
* Persistent error messages only when user action is required

---

# Future Compatibility

Sprint 04 UI is designed so later modules plug into the existing Group Details page without redesign.

Future navigation

```text
Group

│

├── Overview

├── Members

├── Activity

├── Trips (Sprint 05)

├── Expenses (Sprint 06)

├── Settlements (Sprint 07)

├── Reports (Sprint 08)

└── Budgets (Sprint 09)
```

The Group page becomes the collaborative hub for every future sprint.

---

# Frontend Deliverables

Sprint 04 introduces

### Pages

* Groups List
* Create Group
* Join Group
* Group Details
* Members
* Activity Timeline

### Components

* 12+ reusable React components
* Shared dialogs
* Room Code card
* Share button
* Member management components

### Hooks

* 5 Query Hooks
* 7 Mutation Hooks

### UX

* Responsive layouts
* Skeleton loading
* Empty states
* Native sharing
* Clipboard support
* Accessible navigation
* Mobile-first design

---

## Part 4 Complete ✅

The **frontend architecture is now fully specified** and aligned with the backend.

### Before we move to Part 5, I have one architectural recommendation.

I recommend adding a **Group Settings** page now (`/groups/:groupId/settings`) rather than waiting for a later sprint. Even if it initially only contains:

* Group name
* Description
* Currency
* Join by Code (toggle)
* Join by Link (toggle)
* Archive Group
* Delete Group

having this page now avoids overloading the Group Details page later when Trips, Shared Expenses, Settlements, and Budgets are added. It also gives you a dedicated place for future settings without changing the navigation structure. I think it's a worthwhile addition to Sprint 04 because it improves long-term scalability with very little extra implementation effort.



# Part 5 — Security, API Contracts, Validation, Quality Gates & Testing

---

# Security Philosophy

Sprint 04 inherits the complete authentication infrastructure from Sprint 01.

The Group module **must never implement its own authentication mechanism**.

Every API must use the existing:

* JWT Authentication
* Refresh Token
* Spring Security
* UserPrincipal
* Method Security
* Authentication Filters

---

# Authentication

Every endpoint requires an authenticated user.

Example

```java
@AuthenticationPrincipal
UserPrincipal principal
```

Never accept

```text
userId
```

from the frontend.

Always derive the authenticated user from the security context.

---

# Authorization

Group resources are private.

Every request validates ownership or membership.

Example

```text
User

↓

JWT

↓

Authentication

↓

Group Exists?

↓

User Member?

↓

Permission?

↓

Execute
```

If any validation fails

↓

Return

```http
403 Forbidden
```

---

# Resource Ownership

Every service must validate ownership.

Example

```text
Update Group

↓

Owner?

↓

YES

↓

Continue

↓

NO

↓

403
```

The same applies to

* Update Group
* Delete Group
* Archive Group
* Transfer Ownership
* Remove Member
* Promote Member
* Demote Member

---

# Permission Matrix

| Action             |            Owner            |       Admin      |    Member    |
| ------------------ | :-------------------------: | :--------------: | :----------: |
| View Group         |              ✅              |         ✅        |       ✅      |
| Edit Group         |              ✅              |         ✅        |       ❌      |
| Archive Group      |              ✅              |         ❌        |       ❌      |
| Restore Group      |              ✅              |         ❌        |       ❌      |
| Delete Group       |              ✅              |         ❌        |       ❌      |
| Join Group         |              —              |         —        |       —      |
| Leave Group        | ❌ (Must transfer ownership) |         ✅        |       ✅      |
| Remove Member      |              ✅              | ✅ (Members only) |       ❌      |
| Promote Admin      |              ✅              |         ❌        |       ❌      |
| Demote Admin       |              ✅              |         ❌        |       ❌      |
| Transfer Ownership |              ✅              |         ❌        |       ❌      |
| View Activity      |              ✅              |         ✅        |       ✅      |
| Copy Room Code     |              ✅              |         ✅        | Configurable |

---

# Room Code Security

The room code is **not** a secret password.

It is a convenient join mechanism.

Security is provided by

* Large code space
* Authentication
* Membership validation
* Rate limiting

---

## Code Characteristics

* Length: 8 characters
* Uppercase alphanumeric
* Excludes ambiguous characters (`O`, `0`, `I`, `1`)
* Immutable after group creation

Example

```text
X7KD4Q2M
```

---

## Join Protection

The Join API must implement rate limiting.

Recommended

```text
10 attempts

↓

1 minute

↓

Per User

+

Per IP
```

This prevents brute-force attacks.

---

# Share Link Security

The share link simply wraps the room code.

Example

```text
https://expenseflow.app/join/X7KD4Q2M
```

If the group disables link joining

↓

Return

```http
403 Forbidden
```

If the group is archived

↓

Reject joining.

---

# API Design Standards

Every controller returns the standard ExpenseFlow response wrapper.

Success

```json
{
  "success": true,
  "message": "Group created successfully.",
  "data": {}
}
```

Validation Failure

```json
{
  "success": false,
  "message": "Validation failed.",
  "errors": [
    {
      "field": "name",
      "message": "Group name is required."
    }
  ]
}
```

---

# REST API Contract

## Groups

```http
GET    /api/v1/groups

GET    /api/v1/groups/{id}

POST   /api/v1/groups

PATCH  /api/v1/groups/{id}

PATCH  /api/v1/groups/{id}/archive

PATCH  /api/v1/groups/{id}/restore

DELETE /api/v1/groups/{id}
```

Supports

* Pagination
* Sorting
* Search by Name
* Filter by Ownership
* Filter by Membership
* Filter by Archived

---

## Join Group

```http
POST /api/v1/groups/join
```

Request

```json
{
    "groupCode":"X7KD4Q2M"
}
```

Responses

```http
200 Joined Successfully

400 Invalid Code

403 Joining Disabled

404 Group Not Found

409 Already Member
```

---

## Members

```http
GET

/api/v1/groups/{id}/members
```

```http
POST

/api/v1/groups/{id}/leave
```

```http
DELETE

/api/v1/groups/{id}/members/{memberId}
```

```http
PATCH

/api/v1/groups/{id}/members/{memberId}/role
```

```http
POST

/api/v1/groups/{id}/transfer-ownership
```

---

## Activity

```http
GET

/api/v1/groups/{id}/activity
```

Supports

* page
* size
* sort

---

# Validation Rules

## Create Group

Validate

* Name required
* Name length ≤100
* Description ≤300
* Currency required

---

## Join Group

Validate

* Code required
* Code length = 8
* Valid character set
* Group active
* Join enabled
* User not already a member

---

## Leave Group

Validate

* User is active member
* Owner cannot leave
* Group active

---

## Transfer Ownership

Validate

* New owner exists
* Active member
* Not current owner
* Not archived

---

## Remove Member

Validate

* Target exists
* Cannot remove owner
* Admin cannot remove admin
* Member cannot remove anyone

---

# Exception Hierarchy

Create domain-specific exceptions.

```text
GroupNotFoundException

DuplicateGroupCodeException

AlreadyGroupMemberException

InvalidGroupCodeException

JoiningDisabledException

OwnerCannotLeaveException

MemberNotFoundException

InsufficientPermissionException

OwnershipTransferException

ArchivedGroupException
```

Global exception handling remains inside

```text
core.exception
```

---

# Logging

Log

* Group created
* Group archived
* Group restored
* Member joined
* Member removed
* Ownership transferred
* Role changed
* Join failures (without exposing sensitive details)

Never log

* JWT
* User passwords
* Personal notes
* Raw authentication tokens

---

# Performance Requirements

| Endpoint          | Target  |
| ----------------- | ------- |
| Create Group      | <200 ms |
| Join Group        | <200 ms |
| Member List       | <200 ms |
| Activity Timeline | <250 ms |
| Search Groups     | <250 ms |

---

# Testing Strategy

Every vertical feature must include the following.

---

## Unit Tests

Mockito

Cover

* Room code generation
* Group creation
* Join validation
* Permission checks
* Ownership transfer
* Role management

Target

≥80% line coverage

---

## Repository Tests

Use MySQL Testcontainers.

Verify

* Unique room code constraint
* Membership uniqueness
* Soft delete filters
* Activity persistence
* Search queries
* Pagination

---

## Controller Tests

MockMvc

Verify

* Authentication
* Authorization
* Request validation
* API contracts
* HTTP status codes
* Error responses

---

## Integration Tests

Use `@SpringBootTest`.

Verify

* Complete group creation flow
* Join by room code
* Join by share link
* Ownership transfer
* Member removal
* Archive/restore workflow
* Domain event publishing
* Activity logging
* Transaction rollback

---

## Frontend Tests

Vitest

React Testing Library

Verify

* Group list rendering
* Create group form
* Join group form
* Member management
* Activity timeline
* Room code copy
* Share link generation
* Validation messages

---

## End-to-End Tests

Playwright

Verify

* Create group
* Copy room code
* Share link
* Join group
* Leave group
* Transfer ownership
* Promote member
* Remove member
* Archive group

---

## Performance Tests

k6

Verify

* Group APIs
* Join API
* Search
* Member listing
* Activity timeline

Targets

* Error Rate <1%
* P95 latency within SLA
* Stable under concurrent load

---

# Quality Gates

Backend

* JaCoCo Line Coverage ≥80%
* Branch Coverage ≥70%
* PIT Mutation Score ≥75%
* PMD
* SpotBugs
* Checkstyle
* ArchUnit
* Flyway Migration Validation
* Testcontainers

Frontend

* Vitest Coverage ≥80%
* Playwright Passing
* ESLint
* TypeScript Strict Mode

CI/CD

* GitHub Actions
* Dependabot
* CODEOWNERS

Every quality gate must pass before merge.

---

# Security Checklist

Sprint 04 is complete only if

* All APIs require authentication
* Ownership enforced
* Membership validation enforced
* Room codes unique
* Rate limiting enabled
* Soft deletes respected
* Optimistic locking verified
* No entity exposure
* Activity history immutable
* Domain events verified
* Existing Sprint 01–03 functionality unaffected

---

# Regression Testing

Verify Sprint 01

* Registration
* Login
* JWT
* Refresh Tokens
* Password Reset

Verify Sprint 02

* Dashboard
* User Preferences

Verify Sprint 03

* Expenses
* Income
* Categories
* Receipts
* Search
* Timeline

No existing functionality may regress.

---

## Part 5 Complete ✅

Sprint 04 now has:

* Complete backend architecture
* Complete frontend architecture
* Security model
* REST API contracts
* Validation rules
* Testing strategy
* Performance targets
* Quality gates

The only remaining section is **Part 6**, which will finalize the specification with:

* Acceptance Criteria
* Manual Testing Checklist
* Deliverables
* Definition of Done
* Release Notes
* Out-of-Scope items
* Future extensibility
* Final implementation checklist

That will complete the `SPRINT_04.md` document in the same comprehensive style as your Sprint 03 specification.

# Part 6 — Acceptance Criteria, Deliverables & Release

---

# Acceptance Criteria

Sprint 04 is considered complete only when all functional, architectural, security, and quality requirements have been satisfied.

---

# Group Management

The system must allow authenticated users to:

* Create groups.
* Update group information.
* Archive groups.
* Restore archived groups.
* Soft delete groups.
* View group details.
* Search groups.
* View all groups they own or have joined.

Every newly created group must automatically generate:

* A globally unique Room Code.
* A shareable invite link.

The room code must remain immutable for the lifetime of the group.

---

# Group Membership

Users must be able to:

* Join a group using a valid room code.
* Join a group using a valid share link.
* Leave a group.
* View group members.
* View their own role.

The system must prevent:

* Joining the same group twice.
* Joining archived groups.
* Joining when joining by code or link is disabled.
* Joining with invalid room codes.

---

# Role Management

Owners must be able to:

* Promote Members to Admin.
* Demote Admins to Members.
* Transfer Ownership.
* Remove Members.
* Remove Admins.

Admins must be able to:

* Remove Members.
* Update group information (except ownership and deletion).

Members may only:

* View the group.
* Leave the group.

The Owner:

* Cannot remove themselves.
* Cannot leave the group until ownership is transferred.

---

# Group Activity

Every business operation must automatically create an immutable activity record.

Examples include:

* Group created
* Group updated
* Member joined
* Member left
* Member removed
* Role changed
* Ownership transferred
* Group archived
* Group restored
* Group deleted

Activities must:

* Never be edited.
* Never be deleted.
* Always appear in chronological order.

---

# Room Code & Share Link

Every group must have:

* One unique room code.
* One automatically generated share link.

Example

```text
Room Code

X7KD4Q2M

Share Link

https://expenseflow.app/join/X7KD4Q2M
```

The system must guarantee that:

* No duplicate room codes exist.
* Share links always resolve to the correct active group.
* Room codes remain unchanged after group creation.
* Share links become unusable if link joining is disabled.

---

# Non-Functional Requirements

Sprint 04 must satisfy the following engineering standards.

## Performance

| Operation         | Target  |
| ----------------- | ------- |
| Create Group      | <200 ms |
| Join Group        | <200 ms |
| Group Search      | <250 ms |
| Member List       | <200 ms |
| Activity Timeline | <250 ms |

---

## Scalability

The architecture must support future implementation of:

* Trip Workspace
* Shared Expenses
* Expense Split Engine
* Settlement Engine
* Group Reports
* Group Budgets
* AI Features

without requiring redesign of the Group module.

---

## Maintainability

Every new domain must remain independently maintainable.

Architecture

```text
Controllers

↓

Services

↓

Repositories

↓

Entities

↓

Database
```

Cross-domain communication should use domain events.

No future module should directly manipulate Group entities.

---

## Security

Every endpoint must:

* Require authentication.
* Validate membership.
* Validate ownership.
* Respect soft deletes.
* Respect optimistic locking.
* Prevent room code brute-force attacks using rate limiting.
* Never expose internal entities.

---

# Manual Testing Checklist

## Group Lifecycle

* Create group.
* Update group.
* Archive group.
* Restore group.
* Delete group.

---

## Joining

* Join using valid room code.
* Join using share link.
* Invalid room code rejected.
* Duplicate join rejected.
* Joining disabled rejected.
* Archived group rejected.

---

## Membership

* Leave group.
* Owner cannot leave.
* Remove member.
* Remove admin.
* Promote member.
* Demote admin.
* Transfer ownership.

---

## Activity

Verify activity is created for:

* Group creation.
* Member joining.
* Member leaving.
* Member removal.
* Role changes.
* Ownership transfer.
* Archive.
* Restore.

Verify activities remain immutable.

---

## Search

* Search by group name.
* Search by member.
* Pagination.
* Sorting.

---

## Regression Testing

Verify Sprint 01

* Login
* Registration
* JWT
* Refresh Tokens
* Password Reset

Verify Sprint 02

* Dashboard
* User Preferences

Verify Sprint 03

* Expenses
* Income
* Categories
* Receipts
* Search
* Timeline

No existing functionality may regress.

---

# Deliverables

## Backend

Sprint 04 delivers:

* Group module
* Member module
* Activity module
* Room Code generator
* Share Link support
* REST APIs
* DTOs
* MapStruct mappers
* Repository layer
* Service layer
* Domain events

---

## Frontend

Sprint 04 delivers:

* Groups page
* Create Group page
* Join Group page
* Group Details page
* Members page
* Activity page
* Group Settings page
* Responsive layouts
* Native Share integration
* Room Code copy support
* Search
* Skeleton loaders
* Empty states

---

## Database

Flyway Migration

```text
V6__group_management.sql
```

Tables

* groups
* group_members
* group_activity

---

## Testing

* Unit Tests
* Repository Tests
* Controller Tests
* Integration Tests
* Event Tests
* Playwright Tests
* Vitest
* Testcontainers
* k6 Performance Tests

---

# Out of Scope

The following are intentionally excluded from Sprint 04.

## Shared Finance

* Shared Expenses
* Expense Split
* Settlement Engine
* Debt Simplification

---

## Trips

* Trip Workspace
* Trip Participants
* Trip Expenses

---

## Reports

* Group Reports
* Analytics
* Charts

---

## AI

* OCR
* Receipt Scanning
* Smart Insights
* AI Recommendations

---

## Communication

* Email Invitations
* Push Notifications
* SMS
* Chat
* Comments

> **Design Decision:** ExpenseFlow uses **Room Codes** and **Share Links** as the only supported group joining mechanisms in Sprint 04. Email invitations are intentionally omitted to keep onboarding simple and match the primary use cases (friends, families, roommates, trips, and college groups).

---

# Definition of Done

Sprint 04 is complete only if:

* All planned features are implemented.
* Groups can be created successfully.
* Room codes are generated uniquely.
* Share links work correctly.
* Users can join using room code.
* Users can join using share link.
* Membership validation is enforced.
* Ownership rules are enforced.
* Activity history is immutable.
* All APIs require authentication.
* All ownership checks pass.
* Flyway migration executes successfully.
* Static analysis passes.
* Unit tests pass.
* Integration tests pass.
* Playwright tests pass.
* Performance targets are achieved.
* Documentation is complete.
* Code review is approved.

---

# Release Target

**Version**

```text
v0.5.0-group-management
```

---

# Release Notes

Sprint 04 introduces the first collaborative capabilities to ExpenseFlow.

Users can now create groups, manage members, join groups using unique room codes or shareable links, manage permissions through Owner/Admin/Member roles, and track all group activities through an immutable activity timeline.

This sprint establishes the collaboration foundation for future modules including Trip Workspaces, Shared Expenses, Expense Splitting, Settlement Engine, Reports, and Group Budgets. The architecture remains modular, event-driven, and fully backward compatible with Sprints 01–03.

---

# Requirements

The implementation **must**:

* Follow the existing Domain-Driven Architecture.
* Use Java Records for DTOs.
* Use MapStruct for entity mapping.
* Use Flyway for database migrations.
* Use UUIDs for all entity identifiers.
* Use optimistic locking (`@Version`) on editable entities.
* Reuse the existing authentication and authorization infrastructure.
* Generate globally unique 8-character room codes.
* Support joining via room code and share link only.
* Enforce role-based permissions.
* Publish immutable domain events.
* Maintain immutable activity history.
* Never expose entities through controllers.
* Use constructor injection only.
* Enforce resource ownership using the authenticated user.
* Maintain complete backward compatibility with Sprints 01–03.
* Pass all defined quality gates before release.

---

# Sprint Status

**Sprint Name:** Group Management

**Version:** 1.0

**Release:** `v0.5.0-group-management`

**Status:** Ready for Implementation ✅

---

## Overall Review

This Sprint 04 specification is significantly cleaner than the original invitation-based design because it removes the complexity of invitation tables, email workflows, expiration handling, and token management. The **Room Code + Share Link** approach aligns well with ExpenseFlow's target audience and simplifies both the backend and frontend while leaving a clear extension path for future features such as QR codes, Trips, Expense Split, and Settlements. The document now provides a solid foundation for the collaboration features that follow in your roadmap.


