# Splitwise — Expense Sharing

**What this package is:** Interview Question Bank problem H. Shared-expense tracking with pluggable
split schemes, pairwise debt tracking, and minimum-transaction settlement.

Full walkthrough: [`docs/problem-h-splitwise.md`](../../../../../../../docs/problem-h-splitwise.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.problems.splitwise.driver.Driver
```

## Layout

| Subpackage | What it does |
|---|---|
| `entity` | `UserId`, `Expense` (self-validates that shares sum to the total — rich domain model). |
| `strategy` | `SplitStrategy` (OCP) + `EqualSplit`, `ExactSplit`, `PercentSplit` — all conserve money to the exact minor unit. |
| `service` | `BalanceSheet` (thread-safe pairwise ledger + min-cash-flow settlement) and `ExpenseService` orchestrator. |
| `exception` | `InvalidSplitException`. |
| `driver` | Composition root: mixed split types, balances + settlements, then 16k concurrent expenses proving money is conserved. |
