# ADR-004: Money & Currency Policy

## 1. Status
*   **ADR Number:** ADR-004
*   **Status:** ACCEPTED
*   **Version:** 1.2
*   **Sprint:** 06
*   **Supersedes:** None
*   **Depends On:** [ADR-003 Expense Bounded Context Architecture](file:///e:/ExpenseTrack/docs/architecture/ADR-003-Expense-Architecture.md)

---

## 2. Context
Financial calculations require high precision to avoid rounding errors and floating-point inaccuracies. Additionally, splitting expenses (e.g., division) can produce fractional remainders that must be allocated systematically to maintain absolute balancing.

---

## 3. Decision

### 3.1 Data Types
*   All monetary amounts must be represented using `java.math.BigDecimal` (backend) and `string` or `number` (frontend).
*   Floating-point types (`double`, `float` in Java; `number` in Javascript without safe rounding) must **never** be used for financial arithmetic.

### 3.2 Immutability & Equality
*   The `Money` object is a Value Object and must be completely immutable.
*   Any modification (such as addition or subtraction) must create a new instance of `Money`.
*   Money objects are value-equal by amount and currency.

### 3.3 Precision & Scale
*   **Storage Scale**: All database monetary fields must be stored with a scale of 2 (e.g., `DECIMAL(19, 2)`).
*   **Arithmetic Scale**: Intermediate calculations (splits, division) must use a scale of 4 to prevent precision loss.
*   **Final Scale**: The final split shares must be rounded to exactly 2 decimal places.

### 3.4 Rounding Mode
*   All rounding operations must use `RoundingMode.HALF_UP` (nearest-neighbor rounding).

### 3.5 Currency Consistency
*   All transactions within a single `Expense` must be conducted in the same currency.
*   The currency of an `Expense` must use the type-safe `CurrencyCode` enum (`INR`, `USD`, `EUR`, `GBP`) and match the default currency of its parent `Group`. Multi-currency conversion is not supported in the initial version.

### 3.6 Rounding Error Allocation Policy (The "Penny Policy")
When dividing an expense equally leads to a fractional remainder (e.g., splitting $10.00 among 3 people results in $3.33 each, summing to $9.99), the remaining penny ($0.01) must be allocated:
*   The remainder is calculated as: `Remainder = TotalAmount - Sum(RoundedSplits)`.
*   The remainder must be added to the first participant's `owedAmount` inside the `ExpenseSplit` records to ensure `Sum(owedAmount) == TotalAmount` holds true.

---

## 4. Consequences
*   No floating-point bugs or precision loss in calculations.
*   Strict validation: `Sum(owedAmount) == TotalAmount` will always balance.
