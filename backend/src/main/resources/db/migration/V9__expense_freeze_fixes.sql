-- V9: Expense Freeze Fixes - Persist Split Type

ALTER TABLE expenses
ADD COLUMN split_type VARCHAR(20) NOT NULL DEFAULT 'EQUAL';
