# UI_STYLE_GUIDE.md

# ExpenseFlow UI Design System

> This document defines the complete visual language of ExpenseFlow.
>
> Every page, component, and feature must follow this design system.
>
> Consistency is more important than creativity.

---

# 1. Design Philosophy

ExpenseFlow should feel like a modern SaaS application.

It should combine the simplicity of Notion, the polish of Linear, the usability of Google Calendar, and the collaboration experience of Splitwise.

The interface must feel:

* Modern
* Minimal
* Premium
* Fast
* Friendly
* Professional

Avoid making the application look like banking software or spreadsheets.

---

# 2. Design Principles

1. Simple before beautiful.
2. Every action should require the fewest clicks possible.
3. Use whitespace generously.
4. Reduce visual clutter.
5. Prioritize readability.
6. Make common actions obvious.
7. Keep interactions predictable.

---

# 3. Design Style

Primary Style

* Material Design 3

Dashboard Style

* Bento Grid

Landing Page

* Glassmorphism

Trip Workspace

* Timeline Based

Forms

* Clean Material Forms

Animations

* Subtle Motion

Charts

* Modern Flat Design

---

# 4. Color Palette

## Light Theme

Primary

```text
#2563EB
```

Primary Hover

```text
#1D4ED8
```

Secondary

```text
#14B8A6
```

Background

```text
#F8FAFC
```

Surface

```text
#FFFFFF
```

Border

```text
#E2E8F0
```

Text Primary

```text
#0F172A
```

Text Secondary

```text
#64748B
```

Success

```text
#22C55E
```

Warning

```text
#F59E0B
```

Danger

```text
#EF4444
```

Info

```text
#3B82F6
```

---

# 5. Dark Theme

Background

```text
#0F172A
```

Surface

```text
#1E293B
```

Card

```text
#1E293B
```

Border

```text
#334155
```

Text

```text
#F8FAFC
```

Primary colors remain unchanged.

---

# 6. Typography

Primary Font

Inter

Fallback

Roboto

Headings

Bold

Body

Regular

Button Text

Medium

Never use more than two font families.

---

# 7. Border Radius

Buttons

12px

Cards

16px

Dialogs

20px

Inputs

12px

Images

16px

Maintain consistency throughout the application.

---

# 8. Shadows

Cards

Soft shadow

Dialogs

Medium shadow

Dropdowns

Light shadow

Avoid heavy shadows.

---

# 9. Spacing System

Use an 8-point grid.

Spacing values

4

8

12

16

24

32

40

48

64

Avoid arbitrary spacing.

---

# 10. Icons

Use

Material Symbols

or

Lucide React

Never mix icon libraries.

Every icon must have a tooltip where appropriate.

---

# 11. Buttons

Primary

Filled

Secondary

Outlined

Danger

Red Filled

Text Button

Minimal

Floating Action Button

Circular

Large

Primary Color

---

# 12. Forms

Use Material TextFields.

Always include:

Label

Placeholder

Validation

Helper Text

Error Message

Required fields marked clearly.

---

# 13. Cards

Cards should contain:

Title

Optional Icon

Content

Optional Actions

Rounded Corners

Soft Shadow

Consistent Padding

---

# 14. Navigation

Desktop

Left Sidebar

Top Navigation

Mobile

Bottom Navigation

Floating Action Button

Sidebar should collapse on tablet.

---

# 15. Dashboard Layout

Dashboard uses Bento Grid.

Widgets

Current Trips

Monthly Spending

Pending Settlements

Quick Actions

Recent Activity

Budget Overview

Cards should be resizable in future versions.

---

# 16. Trip Workspace

Trip pages use a Timeline Layout.

Sections

Overview

Timeline

Members

Expenses

Budget

Settlement

Reports

Settings

Timeline is the primary navigation inside a trip.

---

# 17. Expense Cards

Every expense card displays:

Category Icon

Title

Amount

Paid By

Participants

Date

Payment Method

Quick Actions

Cards should remain compact and easy to scan.

---

# 18. Empty States

Every empty page should include:

Illustration

Friendly Message

Call To Action

Example

"No expenses yet."

"Click the + button to add your first expense."

---

# 19. Loading States

Use Skeleton Loaders.

Never show blank screens.

Loading indicators should match the surrounding UI.

---

# 20. Dialogs

Dialogs are used for:

Delete Confirmation

Add Group

Create Trip

Invite Members

Settlement Confirmation

Avoid full-screen dialogs unless necessary.

---

# 21. Notifications

Use Snackbar for success messages.

Examples

Expense Added

Trip Created

Settlement Recorded

Use Dialogs only for destructive actions.

---

# 22. Tables

Avoid tables on mobile.

Prefer cards.

Desktop may use tables for reports.

---

# 23. Charts

Use Recharts.

Chart Types

Pie

Bar

Line

Area

Charts should be interactive.

Support tooltips.

---

# 24. Accessibility

Minimum contrast ratio.

Keyboard navigation.

Visible focus states.

Large click targets.

Screen reader friendly labels.

---

# 25. Responsive Breakpoints

Mobile

<640px

Tablet

640–1024px

Desktop

> 1024px

All pages must work on every breakpoint.

---

# 26. Animations

Use Framer Motion.

Animation duration

150–300ms

Allowed Animations

Fade

Slide

Scale

Avoid excessive animations.

---

# 27. Images

Use SVG icons where possible.

Optimize all images.

Lazy load large assets.

---

# 28. Page Structure

Every page follows:

Header

↓

Content

↓

Floating Action Button (if applicable)

↓

Footer (Landing Page only)

Maintain a consistent layout.

---

# 29. Design Consistency Rules

Never invent new button styles.

Never invent new colors.

Never invent new spacing.

Always reuse existing components.

Prefer extending existing components over creating new ones.

---

# 30. UI Quality Checklist

Before completing a sprint:

✓ Responsive

✓ Accessible

✓ Consistent spacing

✓ Correct colors

✓ Correct typography

✓ Loading state

✓ Empty state

✓ Error state

✓ Hover state

✓ Disabled state

✓ Mobile friendly

✓ Matches the design system

Only after all items are satisfied is the UI considered complete.
