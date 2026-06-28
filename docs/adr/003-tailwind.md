# ADR 003: Tailwind CSS and Material UI Styling System

## Status
Approved

## Context
ExpenseFlow requires a highly premium, modern UI with responsive designs, bento grids, and glassmorphism. We need a flexible styling solution that scales without producing massive custom CSS files.

## Decision
We adopt Tailwind CSS for utility-first layouts, responsive structures, and custom glassmorphism design, combined with Material UI v7 components for standard input widgets, tables, and dialogs.

## Consequences
- No inline styling is permitted. Custom styles are managed via Tailwind classes.
- Material UI components will be styled and adapted using Tailwind utilities where appropriate.
- Theme rules (palette, typography, spacing) are synchronized between Tailwind config and the MUI theme provider.
