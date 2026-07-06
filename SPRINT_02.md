# SPRINT_02.md

# Sprint 02 – Personal Expense Management Core

Version: 1.0

Estimated Time: 8–12 Days

Status: Not Started

---

# Sprint Goal

Build the complete personal expense management module for ExpenseFlow.

This sprint establishes the core financial functionality, allowing authenticated users to manage their income, expenses, categories, onboarding preferences, receipts, merchant information, and location-aware transactions.

All financial data must belong exclusively to the authenticated user.

The module should be production-ready and become the foundation for future budgeting, analytics, AI insights, recurring transactions, and collaborative expense groups.

---

# Prerequisites

Completed:

* Sprint 00A
* Sprint 00B
* Sprint 00C
* Sprint 01

Read before implementation:

* MASTER_CONTEXT.md
* ENGINEERING_GUIDELINES.md
* PROJECT_STRUCTURE.md
* UI_STYLE_GUIDE.md
* TECH_STACK.md
* CODING_STANDARDS.md

---

# Sprint Scope

Included

* User Onboarding
* User Financial Profile
* Categories
* Expense Management
* Income Management
* Transaction History
* Receipt Upload
* Merchant Information
* Optional Location Tracking
* Search & Filtering
* Dashboard Summary APIs
* Current Balance Calculation

Not Included

Groups

Trips

Expense Splitting

Budgets

Recurring Transactions

Notifications

OCR

AI Features

Admin Dashboard

Bank Synchronization

Investments

Loans

Subscriptions

Multi-Currency Conversion

---

# Business Objective

After authentication, every user should be able to:

* Complete onboarding
* Configure financial preferences
* Record income
* Record expenses
* Attach receipts
* Save merchant information
* Optionally save expense location
* Search previous transactions
* View dashboard summaries
* Track remaining balance

ExpenseFlow is an expense tracker, not a banking application.

No real bank integrations or payment processing should be implemented.

---

# Frontend Pages

Create

---

/onboarding

Display only after first successful login.

Fields

* Preferred Currency
* Monthly Income (Optional)
* Opening Balance / Current Available Balance (Optional)

Features

* Skip Setup
* Finish Setup
* Complete only once
* Can be edited later from profile

---

/dashboard

Display

* Welcome Message
* Current Balance
* Monthly Income
* Monthly Expenses
* Remaining Balance
* Recent Transactions
* Expense By Category
* Quick Actions

Quick Actions

* Add Expense
* Add Income

---

/expenses

Display

Expense Table

Columns

* Date
* Amount
* Category
* Merchant
* Location Indicator
* Receipt Indicator

Features

* Pagination
* Sorting
* Search
* Filters

---

/expenses/new

Create Expense Form

Fields

* Amount
* Category
* Date
* Description
* Merchant
* Notes
* Tags
* Upload Receipt
* Add Current Location

Features

* Location Permission Request
* Manual Merchant Entry
* Optional Receipt Upload

---

/expenses/:id/edit

Edit Expense

All fields editable.

---

/income

Income History

Display

* Date
* Amount
* Source
* Description

---

/income/new

Fields

* Amount
* Source
* Date
* Description
* Notes

---

/categories

Display

Default Categories

Custom Categories

Features

* Create Category
* Edit Category
* Delete Custom Category

System categories cannot be deleted.

---

/transactions

Unified Transaction History

Display

* Income
* Expenses

Filters

* Date
* Category
* Income
* Expense
* Merchant

---

# Backend Requirements

Entities

UserProfile

Category

Expense

Income

Receipt

---

UserProfile Fields

* id (UUID)
* userId
* preferredCurrency
* monthlyIncome
* openingBalance
* onboardingCompleted
* createdAt
* updatedAt
* createdBy
* updatedBy
* deletedAt
* deletedBy
* isDeleted

---

Category Fields

* id (UUID)
* userId (nullable for system categories)
* name
* icon
* color
* systemCategory
* createdAt
* updatedAt
* createdBy
* updatedBy
* deletedAt
* deletedBy
* isDeleted

---

Expense Fields

* id (UUID)
* userId
* categoryId
* amount
* expenseDate
* merchant
* description
* notes
* latitude
* longitude
* locationName
* address
* receiptId
* tags
* createdAt
* updatedAt
* createdBy
* updatedBy
* deletedAt
* deletedBy
* isDeleted

---

Income Fields

* id (UUID)
* userId
* amount
* source
* incomeDate
* description
* notes
* createdAt
* updatedAt
* createdBy
* updatedBy
* deletedAt
* deletedBy
* isDeleted

---

Receipt Fields

* id (UUID)
* expenseId
* originalFileName
* storedFileName
* fileSize
* mimeType
* storagePath
* uploadedAt

---

# Default Categories

Seed during migration.

* Food
* Shopping
* Transport
* Bills
* Health
* Education
* Entertainment
* Travel
* Salary
* Other

Users may create additional categories.

Default categories cannot be deleted.

---

# APIs

User Profile

GET

/api/v1/profile

POST

/api/v1/profile/onboarding

PUT

/api/v1/profile

---

Categories

GET

/api/v1/categories

POST

/api/v1/categories

PUT

/api/v1/categories/{id}

DELETE

/api/v1/categories/{id}

---

Expenses

GET

/api/v1/expenses

GET

/api/v1/expenses/{id}

POST

/api/v1/expenses

PUT

/api/v1/expenses/{id}

DELETE

/api/v1/expenses/{id}

---

Income

GET

/api/v1/income

GET

/api/v1/income/{id}

POST

/api/v1/income

PUT

/api/v1/income/{id}

DELETE

/api/v1/income/{id}

---

Receipts

POST

/api/v1/receipts

GET

/api/v1/receipts/{id}

DELETE

/api/v1/receipts/{id}

---

Dashboard

GET

/api/v1/dashboard

GET

/api/v1/dashboard/summary

GET

/api/v1/dashboard/category-breakdown

---

Search

GET

/api/v1/search/transactions

Query Parameters

* keyword
* category
* merchant
* fromDate
* toDate
* minAmount
* maxAmount

---

# User Flow

Register

↓

Verify Email

↓

Login

↓

First-Time Onboarding

↓

Dashboard

↓

Add Income

↓

Add Expense

↓

View Dashboard

↓

Search Transactions

↓

View Reports (Summary APIs Only)

---

# Expense Flow

Dashboard

↓

Add Expense

↓

Fill Expense Details

↓

(Optional) Add Merchant

↓

(Optional) Upload Receipt

↓

(Optional) Save Current Location

↓

Save Expense

↓

Dashboard Updates Automatically

---

# Income Flow

Dashboard

↓

Add Income

↓

Fill Income Details

↓

Save

↓

Dashboard Updates Automatically

---

# Merchant & Location Feature

Merchant

Optional free-text field.

Example

* Domino's
* Reliance Fresh
* Amazon
* Local Store

Location

Optional.

User chooses:

* Use Current Location
* Skip

Store

* Latitude
* Longitude
* Location Name
* Address

Location must never be mandatory.

---

# Receipt Feature

Allow attachment of:

* PNG
* JPG
* JPEG
* PDF

Maximum upload size will be defined in backend configuration.

Receipt upload is optional.

Future OCR integration should be supported through clean architecture but is out of scope for this sprint.

---

# Security Requirements

Reuse Sprint 01 authentication.

Every API requires authentication except static resources.

Every financial record must belong to exactly one authenticated user.

A user must never access:

* another user's expenses
* another user's income
* another user's receipts
* another user's profile
* another user's categories

Never trust IDs sent from the frontend.

Always derive the authenticated user from the Security Context.

# Frontend Architecture

Use

* React Hook Form
* Zod
* TanStack Query
* Axios Client
* React Router
* Material UI (Material Design 3)
* React Dropzone (Receipt Upload)
* Browser Geolocation API (Optional Location)

Reuse the authentication architecture from Sprint 01.

All financial APIs must use the authenticated JWT session.

---

# Dashboard Requirements

Dashboard should **not** store any data.

All dashboard widgets must be generated dynamically from the database.

Display

* Current Balance
* Monthly Income
* Monthly Expenses
* Remaining Balance
* Today's Expenses
* Recent Transactions
* Top Spending Categories

Charts

* Expense by Category (Pie)
* Income vs Expense (Bar)
* Monthly Expense Trend (Line)

Dashboard calculations must always reflect the latest transaction data.

---

# Current Balance Calculation

Current Balance is calculated dynamically.

Formula

Opening Balance

*

Total Income

*

Total Expenses

=

Current Balance

Never store Current Balance directly in the database.

Always calculate it from persisted financial data.

---

# Search & Filtering

Users should be able to search transactions by

* Merchant
* Description
* Notes
* Category
* Amount
* Date
* Tags
* Location Name

Filters

* Today
* Yesterday
* This Week
* This Month
* Custom Date Range
* Income
* Expense
* Category
* Amount Range

Search results must always be scoped to the authenticated user.

---

# Validation Rules

Frontend

User Profile

* Currency required
* Monthly income must be positive
* Opening balance cannot be negative

Expense

* Amount required
* Amount > 0
* Category required
* Date required
* Merchant optional
* Notes optional
* Receipt optional
* Location optional

Income

* Amount required
* Amount > 0
* Source required
* Date required

Receipt

* Supported file types only
* Maximum upload size enforced
* Invalid files rejected before upload

---

Backend

Bean Validation

Expense

* Positive amount
* Existing category
* Category belongs to authenticated user or is a system category
* Valid date
* Receipt ownership validation

Income

* Positive amount
* Valid source
* Valid date

Receipt

* File type validation
* File size validation
* Secure file storage
* Filename sanitization

User Profile

* Currency validation
* Monthly income >= 0
* Opening balance >= 0

---

# Business Rules

User Profile

* Onboarding appears only once.
* User may update profile later.
* Currency cannot be null.

Categories

* System categories cannot be edited.
* System categories cannot be deleted.
* User categories can be edited.
* User categories can be soft deleted.
* Duplicate category names are not allowed per user.

Expenses

* Every expense belongs to exactly one user.
* Every expense belongs to one category.
* Receipt is optional.
* Merchant is optional.
* Location is optional.
* Soft delete only.
* Deleted expenses are excluded from dashboard calculations.

Income

* Every income belongs to one user.
* Soft delete only.
* Deleted income is excluded from dashboard calculations.

Receipts

* One receipt per expense.
* Receipt can be replaced.
* Replacing removes the previous file safely.

Dashboard

* Generated dynamically.
* Never persist calculated values.

---

# Error Handling

Display user-friendly messages.

Examples

Expense

* Invalid Amount
* Category Required
* Expense Not Found
* Category Not Found

Income

* Invalid Amount
* Income Not Found

Receipt

* File Too Large
* Unsupported File Type
* Upload Failed

Search

* No Transactions Found

Profile

* Invalid Currency
* Invalid Opening Balance

Do not expose stack traces or internal exception details.

---

# Database Changes

Create Flyway migration

V3__expense_management.sql

Create Tables

* user_profiles
* categories
* expenses
* income
* receipts

Seed Default Categories

* Food
* Shopping
* Transport
* Bills
* Health
* Education
* Entertainment
* Travel
* Salary
* Other

Indexes

* user_id
* category_id
* expense_date
* income_date
* merchant
* created_at

Soft delete support required for all financial tables.

---

# Acceptance Criteria

User completes onboarding.

User profile is persisted.

Default categories are available.

User can create custom categories.

User can edit custom categories.

User cannot edit system categories.

User cannot delete system categories.

User can create an expense.

User can edit an expense.

User can delete an expense.

User can create income.

User can edit income.

User can delete income.

Receipt upload works.

Receipt replacement works.

Merchant information is saved.

Location information is optional and saved correctly.

Dashboard displays correct totals.

Current balance is calculated correctly.

Search returns only authenticated user's transactions.

Filters work correctly.

Responsive UI verified.

No console errors.

No lint errors.

---

# Manual Testing Checklist

Complete onboarding.

Skip onboarding.

Update profile.

Create custom category.

Attempt duplicate category.

Attempt deleting system category.

Add income.

Edit income.

Delete income.

Add expense.

Edit expense.

Delete expense.

Upload receipt.

Replace receipt.

Delete receipt.

Create expense without merchant.

Create expense without location.

Create expense with location.

Create expense with receipt.

Search by merchant.

Search by category.

Search by date.

Search by amount.

Verify dashboard updates after adding income.

Verify dashboard updates after adding expense.

Verify current balance calculation.

Verify deleted records disappear from dashboard.

Reload browser.

Verify persisted data.

Test responsive layout.

---

# Testing Requirements

Backend

* Unit Tests
* Service Tests
* Repository Tests
* Controller Tests
* Integration Tests (Testcontainers MySQL)
* Flyway Migration Tests
* Security Tests
* Authorization Tests

Frontend

* Vitest
* React Testing Library
* Component Tests
* Form Validation Tests
* Dashboard Rendering Tests

E2E

Playwright

Test

* Complete onboarding
* Create category
* Add income
* Add expense
* Upload receipt
* Search transaction
* Dashboard updates
* Logout/Login persistence

Performance

k6

Measure

* Create Expense
* Create Income
* Dashboard Summary
* Search Transactions

Performance Targets

* Dashboard < 300ms
* Create Expense < 500ms
* Create Income < 500ms
* Search < 300ms

---

# Deliverables

Production-ready personal expense management module.

Income management.

Expense management.

User financial profile.

Category management.

Receipt upload.

Location-aware expenses.

Dashboard summary APIs.

Search & filtering.

Reusable financial architecture.

Prepared for future budgeting, recurring transactions, AI insights, and collaborative groups.

---

# Out of Scope

Groups

Trips

Expense Splitting

Settlements

Budgets

Recurring Transactions

Notifications

OCR

AI Insights

Admin Dashboard

Bank Synchronization

Investments

Loans

Subscriptions

Multi-Currency Conversion

---

# Definition of Done

Sprint is complete only if:

User onboarding works.

Profile is persisted.

Categories function correctly.

Income CRUD works.

Expense CRUD works.

Receipt upload works.

Merchant information is stored.

Optional location tracking works.

Dashboard calculations are correct.

Search & filtering work.

Current balance calculation is accurate.

All APIs require authentication.

Users cannot access another user's financial data.

Soft delete works correctly.

Flyway migrations execute successfully.

All automated tests pass.

Static analysis passes.

Coverage gates pass.

No compilation errors.

Ready for Sprint 03.


