# Module 1 — Advanced OOP & SOLID

**What this package is:** the foundational layering skeleton (Entity · Repository · Service ·
Driver) reused by every later scenario, demonstrating SOLID principles and rich-vs-anemic
domain modeling on a simple Order domain.

Full theory: [`docs/module-01-solid.md`](../../../../../../docs/module-01-solid.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.theory.module01solid.driver.Driver
```

## Layout

| Subpackage | What it does |
|---|---|
| `entity` | Rich domain objects — `Order` guards its own invariants; `OrderStatus` holds the transition table; `OrderId` is a strongly-typed record. State changes go through `pay()`/`ship()`/`cancel()`, never setters. |
| `repository` | `OrderRepository` interface + thread-safe `InMemoryOrderRepository` — abstracts persistence (DIP). |
| `strategy` | `ShippingStrategy` with `Standard`/`Express` impls — Open/Closed via polymorphism, not `if/else`. |
| `service` | `OrderService` orchestrates load → mutate-via-domain-method → save, with per-order lock striping for concurrency. |
| `exception` | `IllegalOrderStateException` for rejected transitions. |
| `driver` | Composition root: wires concretes, then races 50 threads to cancel one order (exactly 1 wins). |

**Takeaway:** entities own invariants; services orchestrate; repositories abstract storage;
the driver is the only place that knows concrete types.
