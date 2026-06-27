# Module 3 — SDE-3 Concurrency & State Management

**What this package is:** the concurrency toolkit for LLD interviews — thread-safe singletons,
object pooling, optimistic vs. pessimistic locking, and a side-by-side broken/correct inventory
service that demonstrates a real race condition and its fix.

Full theory: [`docs/module-03-concurrency.md`](../../../../../../docs/module-03-concurrency.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.theory.module03concurrency.driver.Driver
```

## Layout

| Subpackage | What it does |
|---|---|
| `singleton` | Three thread-safe singleton idioms: `HolderSingleton` (init-on-demand), `EnumSingleton`, `DoubleCheckedSingleton` (volatile). |
| `inventory` | `BrokenInventoryService` (lost-update race) vs. `AtomicInventoryService` (CAS-correct) behind one `InventoryService` interface — shows the bug and the fix. |
| `locking` | `OptimisticAccount` (CAS / version-retry) vs. `PessimisticAccount` (lock-based) — the optimistic-vs-pessimistic decision in code. |
| `pool` | `ObjectPool` + `PooledConnection` — bounded resource reuse with safe borrow/return semantics. |
| `driver` | Composition root: drives each demo under concurrent load to show correctness (and the broken case's failure). |
