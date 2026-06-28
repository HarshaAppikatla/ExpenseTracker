# PROJECT_STRUCTURE.md

# ExpenseFlow Project Structure

> This document defines the folder structure, architecture, naming conventions, dependencies, and development organization for the entire ExpenseFlow project.

Every sprint must follow this structure.

Do not introduce new folders unless explicitly required.

---

# 1. Repository Structure

```text
ExpenseFlow/

│
├── frontend/
├── backend/
├── database/
├── docs/
├── .github/
├── README.md
├── docker-compose.yml
└── .gitignore
```

---

# 2. Frontend Structure

```text
frontend/

src/

assets/

components/

common/

layout/

navigation/

ui/

features/

auth/

dashboard/

expenses/

groups/

trips/

settlements/

reports/

profile/

hooks/

layouts/

pages/

routes/

services/

contexts/

types/

utils/

constants/

styles/

App.tsx

main.tsx
```

---

# 3. Backend Structure

```text
backend/

src/main/java/com/expenseflow/

config/

controller/

dto/

entity/

exception/

mapper/

repository/

security/

service/

service/impl/

util/

validation/

ExpenseFlowApplication.java
```

---

# 4. Resource Structure

```text
resources/

application.yml

application-dev.yml

application-prod.yml

messages.properties

db/

migration/
```

---

# 5. Database Folder

```text
database/

schema/

seed/

erd/

sql/

migration/

backup/
```

---

# 6. Documentation Folder

```text
docs/

MASTER_CONTEXT.md

ENGINEERING_GUIDELINES.md

PROJECT_STRUCTURE.md

UI_SYSTEM.md

DATABASE_DESIGN.md

API_SPECIFICATION.md

SPRINTS/
```

---

# 7. React Folder Rules

Pages

Only complete screens.

Example

DashboardPage

LoginPage

TripPage

ExpensePage

Components

Reusable UI only.

Example

Button

Card

Modal

ExpenseCard

TripCard

Avatar

Hooks

Reusable logic only.

Services

API communication only.

No UI code.

Utils

Pure helper functions.

No React code.

---

# 8. Spring Boot Rules

Controller

HTTP Layer

↓

Service

Business Logic

↓

Repository

Database Layer

↓

Database

No shortcuts.

Controllers must never call Repository directly.

---

# 9. Entity Rules

One entity

=

One database table.

Entities should contain

Fields

Relationships

No business logic.

Business logic belongs inside Services.

---

# 10. DTO Rules

Every API

must use DTOs.

Never expose Entity objects.

Create

Request DTO

Response DTO

where appropriate.

---

# 11. Mapper Rules

Entity

↓

DTO

↓

Entity

All conversions happen inside Mapper classes.

Never inside Controllers.

---

# 12. API Response Format

Every API must return

```json
{
  "success": true,
  "message": "Operation successful",
  "data": {},
  "timestamp": "2026-06-28T12:00:00Z"
}
```

No endpoint should return inconsistent responses.

---

# 13. React Component Rules

Each component should contain

Component

Styles

Props Interface

Business logic (minimal)

Never exceed approximately 250 lines in a component.

Split large components.

---

# 14. Feature Module Organization

Every feature follows

```text
feature/

components/

pages/

services/

hooks/

types/

utils/
```

Example

expenses/

components/

ExpenseCard.tsx

ExpenseForm.tsx

pages/

ExpensePage.tsx

services/

expenseService.ts

types/

Expense.ts

hooks/

useExpense.ts

---

# 15. Environment Variables

Frontend

.env

VITE_API_BASE_URL

Backend

application.yml

JWT_SECRET

DATABASE_URL

MAIL_HOST

Never hardcode secrets.

---

# 16. Naming Standards

Components

PascalCase

ExpenseCard.tsx

Variables

camelCase

expenseAmount

Constants

UPPER_CASE

MAX_UPLOAD_SIZE

Packages

lowercase

service

repository

controller

---

# 17. Import Rules

Prefer absolute imports.

Avoid deep relative imports.

Good

@/components/Button

Bad

../../../../Button

---

# 18. Styling Rules

Primary

Tailwind CSS

Components

Material UI

Never use inline CSS.

Avoid custom CSS unless necessary.

Keep styling consistent.

---

# 19. Routing Structure

```text
/

login

register

dashboard

personal

groups

groups/:id

trips

trips/:id

reports

profile

settings
```

Unknown routes

↓

404 Page

---

# 20. Error Pages

Create

401

403

404

500

offline

pages.

Maintain consistent branding.

---

# 21. Loading Components

Create reusable

Loading Spinner

Skeleton Card

Skeleton Table

Skeleton Dashboard

Never display blank pages during loading.

---

# 22. Empty States

Every module should have an Empty State.

Example

No Trips Yet

No Expenses Yet

No Groups Yet

Guide users toward the next action.

---

# 23. Asset Organization

Store

Images

Icons

Logos

Fonts

Illustrations

inside

assets/

Do not mix assets with components.

---

# 24. Testing Structure

Frontend

tests/

components/

pages/

Backend

test/

controller/

service/

repository/

Maintain matching package structure.

---

# 25. Future Expansion

Future modules should be added inside

features/

without modifying existing architecture.

Architecture must remain stable across all future releases.
