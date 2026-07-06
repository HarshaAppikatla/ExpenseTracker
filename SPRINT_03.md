SPRINT_03.md
Sprint 03 – Financial Planning

Version: 1.0

Estimated Time: 7–10 Days

Status: Not Started

Sprint Goal

Build the Financial Planning module for ExpenseFlow.

This sprint extends the personal finance platform by introducing intelligent financial planning capabilities while preserving the integrity of Sprint 01 (Authentication) and Sprint 02 (Personal Finance).

The system should provide users with proactive financial management through:

Monthly Budget Planning
Recurring Transactions
Savings Goals
In-App Notifications
Financial Insights

All new features must remain additive and must not modify existing Expense, Income, Category, Receipt, or Authentication modules.

Financial calculations must always be generated dynamically from transactional data instead of storing redundant aggregates.

Prerequisites

Completed

Sprint 00A
Sprint 00B
Sprint 00C
Sprint 01
Sprint 01D
Sprint 02

Read before implementation

MASTER_CONTEXT.md
ENGINEERING_GUIDELINES.md
PROJECT_STRUCTURE.md
UI_STYLE_GUIDE.md
TECH_STACK.md
CODING_STANDARDS.md
DESIGN_SYSTEM.md
Sprint Scope
Included
Budget Planning
Monthly category budgets
Budget progress
Budget alerts
Budget utilization
Budget history
Recurring Transactions
Recurring expense templates
Recurring income templates
Daily scheduler
Automatic transaction generation
Pause/Resume recurring templates
Savings Goals
Savings goals
Deposit history
Progress tracking
Goal completion detection
Notification Center
In-app notifications
Read notifications
Archive notifications
Notification badge
Notification history
Financial Insights
Monthly comparisons
Income vs Expense trends
Budget utilization
Savings progress
Top spending categories
Not Included
AI Recommendations
Machine Learning
OCR
Email Notifications
Push Notifications
SMS Notifications
CSV Export
PDF Reports
Excel Reports
Group Budgets
Shared Savings
Shared Notifications
Investment Tracking
Tax Reports

Those features belong to future sprints.

Design Principles

Sprint 03 follows five architectural principles.

1. Additive Architecture

No Sprint 02 business logic may be modified.

Existing modules remain stable.

New modules consume existing services instead of replacing them.

2. Event-Driven Communication

Modules communicate through domain events whenever possible.

Example

Expense Created

↓

Budget Module

↓

Budget Check

↓

Notification Event

Avoid direct service-to-service dependencies.

3. Dynamic Calculations

Never store values that can be calculated.

Examples

Do NOT store

Current Budget Usage
Remaining Budget
Savings Progress
Dashboard Totals

Instead calculate them dynamically using SQL aggregate queries.

4. Immutable Financial History

Historical financial data must never be rewritten.

Examples

Savings deposits

Recurring execution history

Generated transactions

Notification history

must remain immutable after creation.

5. Domain Isolation

Each bounded context owns its own business rules.

Budget

↓

Budget Logic

Recurring

↓

Recurring Logic

Savings

↓

Savings Logic

Notification

↓

Notification Logic

Insights

↓

Read-only Analytics

No module may bypass another module's service layer.

High-Level Architecture
Authentication
        │
        ▼
Personal Finance
        │
        ▼
──────────────────────────────
Financial Planning
──────────────────────────────

Budget Engine

Recurring Engine

Savings Engine

Notification Center

Insights Engine

──────────────────────────────

Domain Events

Scheduler

Dynamic SQL Aggregates
Domain-Driven Package Structure
com.expenseflow

├── core
│   ├── audit
│   ├── config
│   ├── event
│   ├── exception
│   ├── notification
│   ├── scheduler
│   ├── security
│   ├── specification
│   ├── storage
│   ├── util
│   ├── validation
│
├── profile
├── category
├── expense
├── income
├── receipt
│
├── budget
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   ├── service
│   └── event
│
├── recurring
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   ├── scheduler
│   ├── service
│   └── event
│
├── savings
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── mapper
│   ├── repository
│   ├── service
│   └── event
│
├── notification
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   ├── service
│   └── listener
│
└── insight
    ├── controller
    ├── dto
    ├── provider
    └── service
Architecture Rules
Use constructor injection only.
Never expose entities directly.
DTOs must be immutable Java Records.
MapStruct handles all mappings.
BigDecimal for all monetary values.
UUIDs for all primary keys.
@Version optimistic locking on editable entities.
Hibernate soft-delete filters remain enabled globally.
All APIs require authenticated users.
Every module publishes domain events instead of invoking unrelated services directly.
Scheduler tasks must reuse existing ExpenseService and IncomeService; they must never bypass validation or repositories.

Part 2 — Database Design & Financial Engine
Database Changes

Create a new Flyway migration.

V5__financial_planning.sql

Sprint 03 introduces five new business domains while preserving complete backward compatibility with Sprint 01 and Sprint 02.

All new tables must follow the existing project conventions.

UUID primary keys
BigDecimal monetary values
Optimistic locking
Soft deletes
Audit fields
Constructor-based entities
Hibernate deleted filters
Database Tables
budgets

Stores monthly spending limits for individual categories.

Budgets are versioned per month, preserving historical financial planning.

Fields
Field	Type	Description
id	UUID	Primary Key
user_id	UUID FK	Owner
category_id	UUID FK	Budget Category
budget_year	INT	Budget Year
budget_month	INT	Budget Month (1–12)
monthly_limit	DECIMAL(19,2)	Maximum planned spending
currency_code	VARCHAR(10)	ISO Currency
alert_percentage	INT DEFAULT 80	Warning threshold
active	BOOLEAN	Enable / Disable
version	BIGINT	Optimistic Lock
audit fields	BaseEntity	Auditing
soft delete fields	BaseEntity	Soft Deletes
Constraints

Unique

(user_id,
 category_id,
 budget_year,
 budget_month)

Only one monthly budget per category.

Indexes
(user_id, budget_year, budget_month)

(user_id, category_id)

(user_id, is_deleted)
recurring_transactions

Stores recurring transaction templates.

These are NOT actual financial transactions.

Scheduler converts templates into real Expense or Income records.

Fields
Field	Type
id	UUID
user_id	UUID FK
transaction_type	ENUM(INCOME, EXPENSE)
category_id	UUID FK Nullable
amount	DECIMAL(19,2)
currency_code	VARCHAR(10)
merchant	VARCHAR(100)
description	VARCHAR(255)
recurrence_type	DAILY / WEEKLY / MONTHLY / YEARLY
recurrence_interval	INT
start_date	TIMESTAMP
next_execution	TIMESTAMP
end_date	TIMESTAMP Nullable
last_execution	TIMESTAMP Nullable
active	BOOLEAN
version	BIGINT
audit fields	BaseEntity
soft delete fields	BaseEntity
Constraints
recurrence_interval >= 1
amount > 0
Indexes
(user_id,
 next_execution)

(user_id,
 active)

(user_id,
 is_deleted)
recurring_execution_history

Tracks every scheduler execution.

Purpose

Prevent duplicate executions
Recovery after restart
Audit recurring jobs
Fields
Field	Type
id	UUID
recurring_transaction_id	UUID FK
generated_transaction_id	UUID
execution_date	TIMESTAMP
execution_status	SUCCESS / FAILED / SKIPPED
error_message	VARCHAR(500) Nullable
created_at	TIMESTAMP
Unique Constraint
(recurring_transaction_id,
 execution_date)

Guarantees idempotency.

Indexes
(recurring_transaction_id)

(execution_status)
savings_goals

Represents long-term savings targets.

Savings progress is never stored.

Progress is calculated dynamically.

Fields
Field	Type
id	UUID
user_id	UUID
title	VARCHAR(100)
description	VARCHAR(255)
target_amount	DECIMAL(19,2)
target_date	TIMESTAMP Nullable
completed	BOOLEAN
completed_at	TIMESTAMP Nullable
version	BIGINT
audit fields	BaseEntity
soft delete fields	BaseEntity
Important Rule

Do NOT store

current_amount

Instead

SUM(savings_deposits.amount)

is always used.

This makes

Savings Deposits

↓

Single Source of Truth

savings_deposits

Immutable ledger.

Every deposit creates one row.

Never edited.

Never overwritten.

Fields
Field	Type
id	UUID
goal_id	UUID FK
amount	DECIMAL(19,2)
deposit_date	TIMESTAMP
notes	VARCHAR(255)
audit fields	BaseEntity
Constraints
amount > 0
Indexes
(goal_id)

(deposit_date)
notifications

Persistent in-app notification feed.

Fields
Field	Type
id	UUID
user_id	UUID
notification_type	VARCHAR(50)
title	VARCHAR(100)
message	VARCHAR(255)
status	UNREAD / READ / ARCHIVED
created_at	TIMESTAMP
Indexes
(user_id,
 status)

(user_id,
 created_at)
shedlock

Supports distributed scheduler locking.

Required for multi-instance deployment.

Fields
Field	Type
name	VARCHAR(64)
lock_until	TIMESTAMP
locked_at	TIMESTAMP
locked_by	VARCHAR(255)
Financial Rules
Budget Formula

Budget Usage

SUM(expenses.amount)

WHERE

category

AND

month

Remaining Budget

Monthly Limit

-

Budget Usage

Progress

(Current Usage

/

Monthly Limit)

×100

Nothing is stored.

Everything is calculated dynamically.

Savings Formula

Current Savings

SUM(savings_deposits.amount)

Progress

SUM(deposits)

/

target_amount

Completion

SUM(deposits)

>=

target_amount

When first satisfied

↓

completed = true

completed_at = NOW()

↓

Publish

SavingsGoalCompletedEvent

Only once.

Budget Alerts

Alert Levels

80%

↓

Warning
90%

↓

Critical
100%

↓

Exceeded

Only one notification per threshold transition.

Recurring Transaction Rules

Recurring Templates

↓

Scheduler

↓

Expense

↓

Audit Event

↓

Notification

↓

Update next_execution

↓

Insert execution history

Scheduler Rules

Never call

LocalDateTime.now()

Reuse

TimeProvider

from Sprint 01.

This keeps scheduler tests deterministic.

Catch-Up Policy

If the scheduler was offline

April

May

June

and today is

July

Scheduler must generate

April

May

June

before advancing to July.

No financial history may be skipped.

Distributed Locking

Use

ShedLock

Scheduler execution must occur exactly once across all application instances.

Core Guarantees

Sprint 03 guarantees

Monthly budgets remain historically accurate.
Savings progress is always derived from immutable deposits.
Recurring transactions are idempotent.
Scheduler recovery is automatic.
Notifications are persistent.
Financial calculations are deterministic.
Existing Sprint 01 and Sprint 02 data models remain unchanged.

Part 3 – Backend Architecture & Domain Implementation
Backend Requirements

Sprint 03 introduces five new bounded contexts.

Each domain owns its own:

Entity
DTOs
Repository
Mapper
Service
Controller
Domain Events
Validation
Tests

Cross-domain communication must occur through domain events instead of direct service dependencies whenever possible.

Budget Module
Entities

Implement

BudgetEntity

Responsibilities

Monthly budget configuration
Category ownership
Alert threshold
Active state
Optimistic locking
Audit support
Soft delete support

BudgetEntity extends

BaseEntity
DTOs

Use immutable Java Records.

BudgetRequest

BudgetResponse

BudgetProgressResponse

BudgetSummaryResponse

Example

public record BudgetRequest(

    UUID categoryId,

    BigDecimal monthlyLimit,

    String currencyCode,

    Integer budgetYear,

    Integer budgetMonth,

    Integer alertPercentage

) {}
Repository
BudgetRepository

Repository responsibilities

CRUD
Monthly lookup
Category uniqueness
Aggregate queries
Budget utilization queries

Example methods

existsByUserAndCategoryAndMonth()

findCurrentMonthBudgets()

findBudgetProgress()

findBudgetSummary()
Service
BudgetService

Responsibilities

Create Budget
Update Budget
Delete Budget
Progress Calculation
Validation
Budget Alerts

Never calculate balances inside controllers.

Controller
BudgetController

Endpoints

GET

/api/v1/budgets

GET

/api/v1/budgets/{id}

POST

PUT

DELETE

GET

/api/v1/budgets/progress

GET

/api/v1/budgets/summary
Recurring Module
Entities
RecurringTransactionEntity

RecurringExecutionHistoryEntity

Execution history is immutable.

Never update history rows.

DTOs
RecurringRequest

RecurringResponse

RecurringExecutionResponse
Repository

Responsibilities

Due templates
Active templates
Execution history
Duplicate prevention

Methods

findDueTransactions()

findActiveTransactions()

existsExecutionHistory()

saveExecutionHistory()
Scheduler Service
RecurringSchedulerService

Responsibilities

Acquire ShedLock
Find due templates
Catch-up execution
Generate Expense/Income
Save execution history
Advance next execution
Publish events

Never access repositories directly.

Reuse

ExpenseService

IncomeService
Scheduler Flow
Acquire Lock

↓

Find Due Templates

↓

For Each Template

↓

History Exists?

↓

Yes

↓

Skip

↓

No

↓

Generate Transaction

↓

Insert History

↓

Publish Event

↓

Advance next_execution

↓

Commit Transaction
Controller
RecurringController

Endpoints

GET

POST

PUT

DELETE

POST

/pause

POST

/resume
Savings Module
Entities
SavingsGoalEntity

SavingsDepositEntity

Deposits are immutable.

Only insert.

Never edit.

DTOs
SavingsGoalRequest

SavingsGoalResponse

SavingsDepositRequest

SavingsDepositResponse

SavingsProgressResponse
Repository

Responsibilities

Goal CRUD
Deposit CRUD
Aggregate progress
Goal completion

Methods

sumDeposits()

findProgress()

findCompletedGoals()
Service

Responsibilities

Create Goal
Deposit Money
Complete Goal
Progress Calculation

Progress

SUM(deposits)

/

Target Amount
Controller

Endpoints

GET

POST

PUT

DELETE

POST

/{id}/deposit

GET

/{id}/progress
Notification Module
Entity
NotificationEntity

Notification lifecycle

UNREAD

↓

READ

↓

ARCHIVED

Never permanently delete notifications.

Archive instead.

DTOs
NotificationResponse

NotificationSummaryResponse
Repository

Methods

findUnread()

findArchived()

markRead()

archive()

countUnread()
Service

Responsibilities

Read
Archive
Count
Pagination

Notification creation occurs only through event listeners.

Event Listeners

Listen for

BudgetExceededEvent

RecurringExecutedEvent

RecurringFailedEvent

SavingsGoalCompletedEvent

Create NotificationEntity

Save

Done

Controller

Endpoints

GET

PUT Read

PUT Read All

PUT Archive

GET Count
Insight Module

Insights are

Read Only.

Never modify business data.

Providers

Split analytics into dedicated providers.

BudgetInsightProvider

CategoryInsightProvider

TrendInsightProvider

SavingsInsightProvider
Insight Service

Acts as Facade.

Coordinates providers.

Returns DTOs.

DTOs
DashboardInsightResponse

MonthlyTrendResponse

BudgetInsightResponse

SavingsInsightResponse

CategoryInsightResponse
Controller
GET

/api/v1/insights/dashboard

GET

/api/v1/insights/trends

GET

/api/v1/insights/categories

GET

/api/v1/insights/budgets

GET

/api/v1/insights/savings
Domain Events

Publish

BudgetCreatedEvent

BudgetUpdatedEvent

BudgetExceededEvent

BudgetDeletedEvent

RecurringTransactionExecutedEvent

RecurringTransactionFailedEvent

SavingsDepositCreatedEvent

SavingsGoalCompletedEvent

NotificationCreatedEvent

Events must be immutable.

Never expose entities inside events.

Only IDs and primitive values.

Validation Rules
DTO Validation

Use Bean Validation

Examples

@NotNull

@NotBlank

@Positive

@PositiveOrZero

@Size

@FutureOrPresent
Service Validation

Validate

User ownership
Soft delete boundaries
Duplicate monthly budgets
Duplicate recurring executions
Valid recurrence intervals
Deposit amount > 0
Goal completion state
Archived notification restrictions
Transaction Management

Every write operation must use

@Transactional

Scheduler execution

↓

Single Transaction

Budget update

↓

Single Transaction

Savings deposit

↓

Single Transaction

Notification generation

↓

Separate Transaction

Mapping Layer

Use

MapStruct

Never map entities manually inside controllers.

Controllers

↓

DTO

↓

Mapper

↓

Entity

↓

Service

↓

Repository

Architectural Rules
Constructor injection only.
No field injection.
No circular dependencies.
Controllers never access repositories.
Services never expose entities.
Events must remain immutable.
All monetary values use BigDecimal.
Time-sensitive logic uses TimeProvider.
Scheduler must be idempotent.
All repositories respect global soft-delete filters.
New modules must not modify Sprint 02 entities directly; interaction should occur through existing services or domain events.

Part 4 – Frontend Architecture & User Experience
Frontend Philosophy

Sprint 03 extends the existing ExpenseFlow interface without introducing a new design language.

All new pages must strictly follow the established Material Design 3 theme and the shared DESIGN_SYSTEM.md.

The Financial Planning module should feel like a natural extension of the Dashboard and Expenses pages built during Sprint 02.

Frontend Stack

Continue using the existing technology stack.

React 19
TypeScript
Vite
Material UI
React Hook Form
Zod
TanStack Query
React Router
Axios API Client
Recharts

No new UI frameworks may be introduced.

Routing

Introduce the following protected routes.

/budgets

/budgets/new

/budgets/:id

/recurring

/recurring/new

/recurring/:id

/savings

/savings/new

/savings/:id

/notifications

/insights

All routes require authentication.

Navigation

Add a new Financial Planning section to the sidebar.

Financial Planning

📊 Budgets

🔁 Recurring

🎯 Savings Goals

🔔 Notifications

📈 Insights

The Notification item displays an unread badge.

Dashboard Enhancements

The Dashboard from Sprint 02 is extended, not redesigned.

Add a new section titled Financial Planning containing compact cards for:

Budget Utilization
Active Savings Goals
Active Recurring Transactions
Unread Notifications

Each card links to its respective module.

Budgets Page

Route

/budgets

Purpose

Manage monthly spending budgets by category.

Page Layout
--------------------------------------------------
Budgets

Track your monthly spending limits.
--------------------------------------------------

Summary Cards

Monthly Budget

Spent

Remaining

Alerts

--------------------------------------------------

Budget Table

Category

Monthly Limit

Current Usage

Remaining

Progress

Status

Actions
--------------------------------------------------
Features
Monthly selector
Year selector
Category filter
Progress bars
Status chips

Status

Normal

Warning

Critical

Exceeded
Budget Form

Fields

Category
Monthly Limit
Currency
Alert Percentage
Budget Month
Budget Year

Validation

Positive amount
Duplicate month/category prevented
Required fields
Recurring Transactions

Route

/recurring

Purpose

Manage scheduled transactions.

Layout
Recurring Transactions

Summary

Templates

Executed This Month

Paused

Next Execution

--------------------------------------------------

Recurring Table
Columns
Transaction Type
Amount
Merchant
Category
Frequency
Next Execution
Status
Actions
Status Chips
ACTIVE

PAUSED

COMPLETED

EXPIRED
Recurring Form

Fields

Income / Expense
Amount
Currency
Merchant
Category
Description
Frequency
Interval
Start Date
End Date
Active
Savings Goals

Route

/savings

Purpose

Track long-term savings goals.

Summary Cards

Goals

Completed

Saved

Remaining

Goal Cards

Each goal displays

Icon
Title
Target Amount
Saved Amount
Remaining Amount
Progress Ring
Days Remaining
Completion Percentage
Deposit Dialog

Fields

Deposit Amount
Date
Notes

Validation

Positive amount
Cannot deposit zero
Required fields
Goal Detail Page

Shows

Timeline
Deposit History
Progress Chart
Remaining Amount
Estimated Completion
Notifications

Route

/notifications

Purpose

Central notification inbox.

Filters
All
Unread
Read
Archived
Notification Card

Display

Icon
Title
Message
Time
Status
Action

Examples

Budget exceeded

2 minutes ago

UNREAD
Actions
Mark Read
Archive
Read All
Filter
Insights

Route

/insights

Purpose

Provide financial analytics.

Read-only.

Dashboard Widgets

Monthly Spending

Income vs Expense

Top Categories

Budget Utilization

Savings Progress

Recurring Transactions

Charts

Use Recharts.

Allowed charts

Line Chart
Area Chart
Bar Chart
Pie Chart
Radial Progress
Stacked Bar

No 3D charts.

No animated charts longer than 300ms.

Shared Components

Create reusable components.

BudgetCard

BudgetProgressBar

BudgetStatusChip

RecurringCard

RecurringStatusChip

SavingsGoalCard

SavingsProgressRing

DepositDialog

NotificationCard

InsightCard

TrendIndicator

AnalyticsHeader
State Management

Use TanStack Query.

Separate

Queries

Mutations

Example

Queries

useBudgets()

useRecurring()

useSavings()

useNotifications()

useInsights()

Mutations

useCreateBudget()

useUpdateBudget()

useDeleteBudget()

useDeposit()

usePauseRecurring()

useResumeRecurring()

useReadNotification()
Form Validation

React Hook Form

Zod

Example

Budget

↓

Positive Limit

Recurring

↓

Valid Interval

Savings

↓

Positive Deposit

Notification

↓

No validation required
Loading States

Every page must provide

Skeleton Cards
Skeleton Tables
Skeleton Charts

Never use full-page spinners.

Empty States

Examples

Budgets

📊

No budgets created yet.

Create your first monthly budget.

[ Create Budget ]

Recurring

🔁

No recurring transactions.

Schedule your first recurring payment.

[ Create Recurring ]

Savings

🎯

No savings goals.

Start saving for your next goal.

[ Create Goal ]

Notifications

🔔

You're all caught up.

No notifications available.
Responsive Design

Desktop

Full dashboard
Multi-column grids
Data tables

Tablet

Two-column layouts
Compact cards

Mobile

Single-column layouts
Card-based lists
Bottom sheets instead of large dialogs
Collapsible filters
UI & UX Requirements

Every page must support:

Consistent spacing
Skeleton loading
Empty states
Error states
Success snackbars
Confirmation dialogs for destructive actions
Keyboard accessibility
WCAG AA color contrast
Responsive layouts
Smooth animations (≤ 200ms)
Performance Requirements
Lazy-load Financial Planning routes.
Memoize expensive chart components.
Debounce search inputs.
Paginate large datasets.
Virtualize lists where appropriate (e.g., long notification feeds).
Cache dashboard and insight queries with TanStack Query.
Frontend Deliverables

Sprint 03 introduces:

6 new pages
10+ reusable UI components
5 TanStack Query modules
Responsive layouts for desktop, tablet, and mobile
Charts, progress indicators, notifications, and planning dashboards
A consistent UX aligned with the existing ExpenseFlow design system

Part 5 – Security, API Contracts, Validation, Quality Gates & Testing
Security Requirements

Sprint 03 inherits all authentication and authorization mechanisms from Sprint 01.

No Sprint 03 feature may bypass the established security layer.

Every endpoint must require a valid authenticated user unless explicitly documented otherwise.

Authentication

Continue using the existing authentication infrastructure.

JWT Access Token
Refresh Token
Spring Security
Method Security
UserPrincipal
Authentication Filters

Sprint 03 must never implement a separate authentication mechanism.

Authorization

Every Financial Planning resource is private to its owner.

All queries must be scoped using the authenticated user.

Example

@AuthenticationPrincipal UserPrincipal principal

Never accept a userId from the frontend.

Always derive ownership from the security context.

Resource Ownership

Every service must verify ownership before performing any operation.

Example

Budget

↓

belongs to authenticated user?

↓

YES

↓

Continue

↓

NO

↓

403 Forbidden

The same rule applies to:

Budgets
Recurring Transactions
Savings Goals
Deposits
Notifications
Insights
Scheduler Security

Recurring jobs execute without a logged-in user.

Instead, the scheduler must use a dedicated System Execution Context.

Characteristics

Internal only
Not exposed through APIs
Used exclusively by scheduled jobs
Writes audit information as SYSTEM

Scheduler-generated transactions must still pass through the existing service layer.

Never access repositories directly.

API Design Standards

All APIs follow the ExpenseFlow REST conventions.

Controllers

↓

DTOs

↓

Services

↓

Repositories

↓

Entities

Controllers never return entities.

API Response Format

Every successful response should use the existing API response wrapper.

Example

{
  "success": true,
  "message": "Budget created successfully.",
  "data": { ... }
}

Validation failures should return consistent error payloads.

Example

{
  "success": false,
  "message": "Validation failed.",
  "errors": [
    {
      "field": "monthlyLimit",
      "message": "Monthly limit must be greater than zero."
    }
  ]
}
REST API Contract
Budget APIs
GET
/api/v1/budgets

Returns paginated monthly budgets.

Supports

page
size
sort
month
year
category
POST
/api/v1/budgets

Creates a monthly budget.

PUT
/api/v1/budgets/{id}

Updates an existing budget.

DELETE
/api/v1/budgets/{id}

Soft delete.

GET
/api/v1/budgets/progress

Returns

Usage
Remaining
Progress
Status
GET
/api/v1/budgets/summary

Returns dashboard summary cards.

Recurring APIs
GET

/api/v1/recurring

Supports pagination.

POST

/api/v1/recurring

Create template.

PUT

/api/v1/recurring/{id}

Update template.

DELETE

/api/v1/recurring/{id}

Soft delete.

POST

/api/v1/recurring/{id}/pause

Pause scheduler.

POST

/api/v1/recurring/{id}/resume

Resume scheduler.

GET

/api/v1/recurring/history/{id}

Execution history.

Savings APIs
GET

/api/v1/savings

Goals list.

POST

/api/v1/savings

Create goal.

PUT

/api/v1/savings/{id}

Update goal.

DELETE

/api/v1/savings/{id}

Soft delete.

POST

/api/v1/savings/{id}/deposit

Create deposit.

GET

/api/v1/savings/{id}/progress

Dynamic progress.

GET

/api/v1/savings/{id}/history

Deposit history.

Notification APIs
GET

/api/v1/notifications

Supports

page
size
status
PUT

/api/v1/notifications/{id}/read
PUT

/api/v1/notifications/read-all
PUT

/api/v1/notifications/{id}/archive
GET

/api/v1/notifications/unread-count

Returns badge count.

Insight APIs
GET

/api/v1/insights/dashboard
GET

/api/v1/insights/trends
GET

/api/v1/insights/categories
GET

/api/v1/insights/budgets
GET

/api/v1/insights/savings
Validation Rules
Budget

Validate

Category required
Positive limit
Valid month
Valid year
Currency required
Duplicate monthly budget prohibited
Recurring

Validate

Positive amount
Valid recurrence interval
Future start date
End date after start date
Category required for expenses
Merchant optional
Savings

Validate

Goal title required
Positive target amount
Positive deposit amount
Goal not deleted
Goal not archived
Notifications

Validate

Ownership
Valid state transition

Allowed

UNREAD

↓

READ

↓

ARCHIVED

Not allowed

ARCHIVED

↓

UNREAD
Error Handling

Every module must throw domain-specific exceptions.

Examples

BudgetAlreadyExistsException

BudgetExceededException

RecurringExecutionException

SavingsGoalCompletedException

DuplicateRecurringExecutionException

NotificationAlreadyArchivedException

Global exception handling remains inside

core.exception

Controllers never catch exceptions.

Scheduler Execution Policy

Recurring Scheduler

Runs daily.

Responsibilities

Acquire ShedLock
Find due templates
Process overdue executions
Persist execution history
Publish events
Advance next execution

Execution must be transactional.

Catch-Up Policy

If the server was unavailable,

the scheduler must generate all missed executions until the schedule is current.

Example

Daily Recurring

Server Offline

3 Days

↓

Three Expenses Generated

↓

History Saved

↓

next_execution Updated

No financial history may be skipped.

Performance Requirements

Budget summary

< 200 ms

Dashboard insights

< 300 ms

Recurring execution lookup

< 300 ms

Notifications

< 200 ms

Savings progress

< 200 ms

Logging

Log

Scheduler start
Scheduler finish
Budget exceeded
Recurring generated
Recurring skipped
Goal completed
Notification created

Never log

JWT
Passwords
Personal financial notes
Receipt paths
Formalized Test Strategy

Every vertical slice must include the following.

Unit Tests

Mockito

Cover

Budget calculations
Scheduler services
Savings progress
Notification lifecycle
Insight providers
Event publishers
Repository Tests

Use

MySQL Testcontainers

Verify

Aggregate queries
Dynamic sums
Unique constraints
Soft deletes
Scheduler history
Monthly budgets
Controller Tests

MockMvc

Verify

Request validation
Security
Ownership
Status codes
API contracts
Integration Tests

SpringBootTest

Verify

Scheduler execution
Event publishing
Notification generation
Budget calculations
Savings completion
Catch-up policy
ShedLock behavior
Duplicate prevention
Frontend Tests

Vitest

React Testing Library

Verify

Budget pages
Savings pages
Recurring pages
Notification center
Insights dashboard
Charts
Forms
Validation
End-to-End Tests

Playwright

Verify

Budget workflow
Recurring workflow
Scheduler-generated transactions
Savings deposits
Goal completion
Notification lifecycle
Dashboard updates
Performance Tests

k6

Verify

Budget APIs
Scheduler performance
Notification APIs
Insights APIs
Savings APIs

Target

Error Rate < 1%
P95 latency within defined SLAs
Stable performance under concurrent load
Quality Gates

Sprint 03 must satisfy the existing project standards.

Backend

JaCoCo Line Coverage ≥ 80%
Branch Coverage ≥ 70%
PIT Mutation Score ≥ 75%
PMD
SpotBugs
Checkstyle
ArchUnit
Flyway Migration Validation
Testcontainers Integration Tests

Frontend

Vitest Coverage ≥ 80%
Playwright E2E
ESLint
TypeScript Strict Mode

CI/CD

GitHub Actions
Dependabot
CODEOWNERS
All quality gates must pass before merge
Security Checklist

Before Sprint 03 is considered complete:

All APIs authenticated
Ownership enforced
No direct entity exposure
Soft deletes respected
Optimistic locking verified
Scheduler idempotent
Distributed lock tested
Event publishing verified
SQL aggregates validated
Existing Sprint 01 & Sprint 02 functionality unaffected

Part 6 – Acceptance Criteria, Deliverables & Release
Acceptance Criteria

Sprint 03 is considered complete only when all functional, architectural, security, and quality requirements have been satisfied.

Budget Management

The system must allow authenticated users to:

Create monthly budgets.
Update existing budgets.
Soft delete budgets.
View budget progress.
View budget summaries.
Receive alerts when configured thresholds are exceeded.

Budget calculations must always be generated dynamically using database aggregate queries.

No budget usage values may be persisted.

Monthly budgets must remain historically accurate.

Recurring Transactions

The system must allow authenticated users to:

Create recurring transaction templates.
Edit templates.
Pause templates.
Resume templates.
Delete templates.

The scheduler must:

Generate actual Expense or Income records.
Never create duplicate executions.
Recover correctly after application downtime.
Record every execution in execution history.
Publish domain events.
Respect optimistic locking.

Recurring templates are never treated as financial transactions.

Savings Goals

Users must be able to:

Create savings goals.
Update goals.
Soft delete goals.
Add deposits.
View complete deposit history.
Track savings progress.

Savings progress must always be calculated dynamically from immutable deposit records.

Goal completion must publish a domain event exactly once.

Notification Center

Users must be able to:

View notifications.
Filter notifications.
Mark notifications as read.
Archive notifications.
View unread notification count.

Notifications must only be generated through domain events.

No module may insert notifications directly except the Notification module.

Financial Insights

The Insights module must provide read-only analytical data including:

Income vs Expense trends
Monthly comparisons
Budget utilization
Savings progress
Top spending categories
Dashboard summary metrics

Insights must be generated using SQL aggregate queries.

No analytical data may be persisted.

Non-Functional Requirements

Sprint 03 must satisfy the following engineering standards.

Performance

Average response targets:

Endpoint	Target
Budget Summary	< 200 ms
Budget Progress	< 200 ms
Savings Progress	< 200 ms
Notifications	< 200 ms
Dashboard Insights	< 300 ms
Scheduler Execution	< 500 ms
Scalability

The architecture must support:

Multiple application instances
Distributed scheduler execution
Future notification channels
Future reporting modules
Future AI modules
Future group financial planning

without requiring redesign.

Maintainability

Every new domain must remain independently maintainable.

Controllers

↓

Services

↓

Repositories

↓

Entities

↓

Database

Cross-domain communication should use domain events.

Security

Every API must

Require authentication.
Validate ownership.
Prevent ID enumeration.
Respect soft deletes.
Respect optimistic locking.
Reuse existing security infrastructure.
Manual Testing Checklist
Budgets
Create budget.
Update budget.
Delete budget.
Duplicate monthly budget rejected.
Budget progress updates after new expense.
Budget alerts appear correctly.
Recurring
Create recurring expense.
Create recurring income.
Pause template.
Resume template.
Scheduler generates transaction.
Scheduler catch-up after downtime.
Duplicate execution prevented.
Savings
Create goal.
Deposit money.
Progress updates correctly.
Completion notification generated.
Deposit history remains immutable.
Notifications
Budget alert notification.
Savings completion notification.
Recurring execution notification.
Mark notification as read.
Archive notification.
Badge count updates correctly.
Insights
Dashboard summary loads.
Monthly comparison accurate.
Budget utilization accurate.
Savings chart accurate.
Category analytics accurate.
Regression Testing

Verify Sprint 01

Login
Registration
JWT
Refresh Tokens
Email Verification
Password Reset

Verify Sprint 02

Dashboard
Expenses
Income
Categories
Receipts
Search
Merchant Suggestions
Geolocation
Transactions

No existing functionality may regress.

Deliverables

Sprint 03 delivers:

Backend
Budget module
Recurring module
Savings module
Notification module
Insight module
Scheduler engine
ShedLock integration
Execution history
Domain events
REST APIs
Repository layer
Service layer
DTOs
MapStruct mappers
Frontend
Budgets page
Recurring page
Savings page
Notifications page
Insights page
Dashboard financial widgets
Responsive layouts
Charts
Forms
Empty states
Skeleton loaders
Database

Flyway Migration

V5__financial_planning.sql

Including:

budgets
recurring_transactions
recurring_execution_history
savings_goals
savings_deposits
notifications
shedlock
Testing
Unit Tests
Repository Tests
Controller Tests
Integration Tests
Scheduler Tests
Event Tests
Playwright
Vitest
Testcontainers
k6 Performance Tests
Out of Scope

The following are explicitly excluded from Sprint 03.

Artificial Intelligence
Spending prediction
Smart recommendations
AI budgeting
AI insights
Chat assistant
Reports
CSV Export
Excel Export
PDF Reports
Printable Statements
Notification Channels
Email
SMS
Push Notifications
WhatsApp
Slack
Investments
Stocks
Mutual Funds
Crypto
Fixed Deposits
Group Planning
Shared Budgets
Shared Savings Goals
Group Notifications
Group Recurring Transactions

These belong to future sprints.

Definition of Done

Sprint 03 is complete only if:

All planned features are implemented.
Budget calculations are accurate.
Savings calculations are dynamic.
Scheduler execution is idempotent.
Scheduler recovery works correctly.
Notifications are generated through events.
Insights match SQL aggregates.
All APIs require authentication.
All ownership checks pass.
No Sprint 01 functionality is broken.
No Sprint 02 functionality is broken.
Flyway migrations execute successfully.
No compiler warnings remain.
Static analysis passes.
Mutation testing passes.
Integration tests pass.
Performance tests satisfy defined SLAs.
Documentation is complete.
Code review is approved.
Release Target

Version

v0.4.0-financial-planning
Release Notes

Sprint 03 transforms ExpenseFlow from a personal expense tracker into a comprehensive financial planning platform.

Users can now define monthly budgets, automate recurring income and expenses, manage savings goals, receive intelligent in-app financial notifications, and gain actionable insights through dynamic analytics. The architecture remains event-driven, domain-oriented, and backward compatible with Sprint 01 (Authentication) and Sprint 02 (Personal Finance), ensuring a scalable foundation for future collaboration, reporting, AI features, and shared financial workspaces.

Requirements:

Follow the existing domain-driven architecture.
Use Java Records for DTOs.
Use MapStruct for entity mappings.
Use Flyway for database migrations.
Use BigDecimal for all monetary values.
Use UUIDs for all entity identifiers.
Use optimistic locking (@Version) for editable entities.
Reuse the existing authentication and authorization infrastructure.
Reuse ExpenseService and IncomeService for scheduler-generated transactions.
Use TimeProvider for all time-sensitive logic.
Use ShedLock for distributed scheduler execution.
Store recurring execution history to guarantee idempotency.
Calculate budgets, savings progress, and insights dynamically using SQL aggregate queries.
Generate notifications exclusively through domain events.
Never expose entities directly through controllers.
Use constructor injection only.
Ensure all new APIs require authenticated users and enforce resource ownership.
Maintain full backward compatibility with Sprint 01 and Sprint 02.
Implement comprehensive unit, repository, controller, integration, frontend, end-to-end, and performance tests.
Do not implement AI features, exports, reports, email notifications, push notifications, investment tracking, or group financial planning.
Stop after Sprint 03 is fully implemented, validated, documented, and all quality gates pass.
Sprint Status

Sprint Name: Financial Planning

Version: 1.0

Release: v0.4.0-financial-planning

Status: Ready for Implementation ✅