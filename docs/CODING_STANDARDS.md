# ExpenseFlow Coding Standards

This document establishes the repository-wide coding conventions and practices. All developers and AI assistants must follow these standards.

---

## 1. Java / Spring Boot Standards

### Dependency Injection
- **Rule:** Always use **Constructor Injection** via Lombok's `@RequiredArgsConstructor`.
- **Reason:** Promotes immutability, eases testing, and avoids partial initialization states.
- **Rule:** Never use Field Injection (`@Autowired` on variables).
- **Rule:** Define all injected services/repositories as `private final`.

### null-Safety and Optionals
- **Rule:** Return `Optional<T>` from service methods and repository methods that can return `null`.
- **Rule:** Avoid passing `Optional` as method parameters. Use method overloading or default values.
- **Rule:** Always check or handle empty Optionals (`.orElseThrow()`, `.map()`, `.ifPresent()`). Never call `.get()` without verification.

### API Response Wrappers
- **Rule:** All controller endpoints must return `ResponseEntity<ApiResponse<T>>`.
- **Rule:** Always encapsulate data in DTOs. Never return entities directly in controllers.
- **Rule:** Validate inputs using `@Valid` annotations and handle mapping errors globally.

---

## 2. React / TypeScript Standards

### Type Safety
- **Rule:** Never use `any`. Specify exact types or use generics.
- **Rule:** Always use `interface` for defining component props and API responses rather than `type`, unless specifying union types or aliases.
- **Rule:** Maintain strict mode configurations (`"strict": true` in tsconfig).

### Components and Rendering
- **Rule:** Avoid inline functions inside `.map()` rendering. Define them as helper functions or components.
- **Rule:** Memoize expensive computations with `useMemo` and callbacks with `useCallback` when passing to optimized children.
- **Rule:** Use absolute imports (`@/components/...`) rather than deep relative paths (`../../../../...`).
- **Rule:** Component sizes must not exceed 250 lines. Extract sub-components for complex DOM trees.

### Data Fetching
- **Rule:** Never execute raw `axios` or `fetch` calls directly inside components.
- **Rule:** All server interactions must use custom queries or mutations via **TanStack Query** (React Query).

---

## 3. SQL / Database Standards

### Schema and Queries
- **Rule:** Never use `SELECT *`. Always specify required column names.
- **Rule:** Foreign keys must be indexed to avoid sequential table scans.
- **Rule:** Use UUIDs (stored as standard CHAR(36) or BINARY(16)) as primary keys across all modules.

### Financial and Audit Consistency
- **Rule:** Never perform hard-deletes on financial records, settlement engines, or user transactions. Use soft-deletes (`deleted_at`, `deleted_by`, `is_deleted`).
- **Rule:** Never configure `ON DELETE CASCADE` for transaction history or balance statements. Destructive operations should be prevented or handled in business service methods.

---

## 4. Git Strategy

### Commits and PRs
- **Rule:** Use structured prefixes for commits: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`.
- **Rule:** One feature $\rightarrow$ One Commit $\rightarrow$ One PR. Keep commits atomic and self-contained.
- **Rule:** Add corresponding issue IDs or sprint labels to pull requests.
