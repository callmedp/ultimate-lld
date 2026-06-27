# Module 2 — Core Design Patterns

**What this package is:** one canonical, framework-agnostic implementation of each of the
eight core Gang-of-Four patterns most asked about in SDE-3 LLD interviews. Every pattern is
real, runnable code (no stubs), wired and exercised by `driver/Driver`.

Full theory + Mermaid diagrams + pitfalls: [`docs/module-02-patterns.md`](../../../../../../docs/module-02-patterns.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.theory.module02patterns.driver.Driver
```

## Layout (subpackage → pattern)

| Subpackage | Pattern | What it does | Key files |
|---|---|---|---|
| `creational` | **Factory** | Builds a `Notification` (Email/SMS/Push) by channel so callers depend on the interface, not concretes. | `NotificationFactory`, `Notification` |
| `creational` | **Builder** | Constructs an immutable `HttpRequest` with required + optional fields; all cross-field validation in `build()`. | `HttpRequest` |
| `creational` | **Singleton** | Shows three idioms — holder, double-checked-locking, enum — plus a thread-safe `FeatureRegistry` config the driver hammers from 32 threads. | `HolderSingleton`, `EnumSingleton`, `DoubleCheckedSingleton`, `FeatureRegistry` |
| `structural` | **Decorator** | Wraps a `DataSource` with stackable compression + encryption at runtime; round-trips correctly. | `DataSourceDecorator`, `CompressionDecorator`, `EncryptionDecorator` |
| `structural` | **Facade** | Collapses reserve-stock → charge → ship behind one `checkout()` call, with compensating rollback. | `CheckoutFacade` (+ `InventoryService`, `PaymentGateway`, `ShippingService`) |
| `behavioral` | **Strategy** | Interchangeable pricing algorithms (regular / discount / surge) selected at runtime. | `PricingStrategy`, `PriceCalculator` |
| `behavioral` | **Observer** | Thread-safe one-to-many event fan-out via `CopyOnWriteArrayList`; driver publishes from 16 threads. | `Subject`, `Observer`, `CountingObserver` |
| `behavioral` | **State** | Vending-machine state machine where each state is its own class handling transitions. | `VendingMachine`, `IdleState`, `HasMoneyState`, `DispensingState` |
| `driver` | — | Composition root: wires everything and demonstrates every pattern, including the two concurrency demos. | `Driver` |

## Conventions
Rich domain objects, fail-fast constructors (`Objects.requireNonNull`), defensive copies,
`final` classes, money as `long` minor units (reuses `common.Money`), OCP via interfaces,
DIP via constructor injection, thread-safety via concurrent collections / atomics / locks.
