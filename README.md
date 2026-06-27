# Ultimate LLD — SDE-3 Low-Level Design in Java

A self-paced, production-grade curriculum for **Low-Level Design (LLD)** in Java, built
for senior (SDE-3) interview preparation. Framework-agnostic: no MVC, no Spring — pure
**OOD, SOLID, GoF patterns, and DDD** (Entities · Services · Repositories), with
SDE-3-level **concurrency and thread-safety** throughout.

## Quick start

Requires JDK 17+. Gradle is bundled via the wrapper — no install needed.

```bash
# Run a module / scenario driver (defaults to Module 1):
./gradlew run

# Run a specific driver explicitly:
./gradlew run -Pdriver=com.ultimatelld.theory.module01solid.driver.Driver

# Compile everything / run tests:
./gradlew build
./gradlew test
```

## Architectural standards (enforced in every sample)

| Layer | Responsibility |
|-------|----------------|
| **Entity** (`*/entity`) | Rich domain objects: encapsulated state **+** behavior that guards invariants. No anemic data bags. |
| **Service** (`*/service`) | Orchestrators: load → invoke domain method → persist → notify. Hold design patterns + concurrency control. |
| **Repository** (`*/repository`) | Interfaces abstracting persistence; in-memory impls use thread-safe collections (`ConcurrentHashMap`). |
| **Driver** (`*/driver`) | Composition root: the only place that wires concrete types, simulates concurrent clients. |

Cross-cutting rules: constructor-based **DI** only, **SOLID** throughout, new features added
via **new classes** (Open/Closed), all shared mutable state guarded by `ReentrantLock` /
atomics / concurrent collections.

## Project layout

```
ultimate-lld/
├── build.gradle.kts          # Gradle build (Java 17 toolchain, JUnit 5)
├── gradlew / gradlew.bat     # Gradle wrapper — no local Gradle needed
├── docs/                     # Theory, diagrams, walkthroughs (Markdown)
│   ├── 00-curriculum-outline.md
│   └── module-01-solid.md
└── src/main/java/com/ultimatelld/
    ├── common/               # Shared value objects (Money, ...)
    ├── theory/               # Section 1 — core LLD theory
    │   ├── module01solid/         # Module 1 — SOLID + layering skeleton
    │   ├── module02patterns/      # Module 2 — core GoF design patterns
    │   └── module03concurrency/   # Module 3 — concurrency & state management
    └── problems/             # Section 2 — interview question bank (named by problem)
        ├── moviebooking/          # Concurrent movie ticket booking
        ├── ratelimiter/           # Distributed rate limiter / throttling
        ├── parkinglot/            # High-throughput parking lot
        ├── taskscheduler/         # Task scheduler / job queue
        ├── pubsub/                # In-memory pub-sub messaging
        ├── lrucache/              # Thread-safe LRU/LFU cache
        ├── elevator/              # Multi-car elevator system
        └── splitwise/             # Expense sharing
```

Each leaf package follows the same internal layout: `entity/ repository/ strategy/ service/
driver/ exception/` (as applicable). `theory/` holds the reusable principles; `problems/`
applies them to full interview problems.

## Curriculum progress — ✅ all complete

Every module/scenario has runnable code (`*/driver/Driver.java`) and a docs file. Each driver
below has been compiled and executed; the verified output is summarized in its docs page.

### Section 1 — Core Theory
- [x] **Module 1** — Advanced OOP & SOLID  ·  [docs](docs/module-01-solid.md)
  `./gradlew run -Pdriver=com.ultimatelld.theory.module01solid.driver.Driver`
- [x] **Module 2** — Core Design Patterns  ·  [docs](docs/module-02-patterns.md)
  `./gradlew run -Pdriver=com.ultimatelld.theory.module02patterns.driver.Driver`
- [x] **Module 3** — SDE-3 Concurrency & State Management  ·  [docs](docs/module-03-concurrency.md)
  `./gradlew run -Pdriver=com.ultimatelld.theory.module03concurrency.driver.Driver`

### Section 2 — Elite Interview Problem Bank
- [x] **Scenario A** — Concurrent Movie Ticket Booking  ·  [docs](docs/scenario-a-movie-booking.md)
  `./gradlew run -Pdriver=com.ultimatelld.problems.moviebooking.driver.Driver`
- [x] **Scenario B** — Distributed Rate Limiter  ·  [docs](docs/scenario-b-rate-limiter.md)
  `./gradlew run -Pdriver=com.ultimatelld.problems.ratelimiter.driver.Driver`
- [x] **Scenario C** — High-Throughput Parking Lot  ·  [docs](docs/scenario-c-parking-lot.md)
  `./gradlew run -Pdriver=com.ultimatelld.problems.parkinglot.driver.Driver`
- [x] **Scenario D** — Task Scheduler / Job Queue  ·  [docs](docs/scenario-d-task-scheduler.md)
  `./gradlew run -Pdriver=com.ultimatelld.problems.taskscheduler.driver.Driver`
- [x] **Scenario E** — In-Memory Pub-Sub Messaging  ·  [docs](docs/scenario-e-pubsub.md)
  `./gradlew run -Pdriver=com.ultimatelld.problems.pubsub.driver.Driver`
- [x] **Problem F** — Thread-Safe LRU/LFU Cache  ·  [docs](docs/problem-f-lru-cache.md)
  `./gradlew run -Pdriver=com.ultimatelld.problems.lrucache.driver.Driver`
- [x] **Problem G** — Multi-Car Elevator System  ·  [docs](docs/problem-g-elevator.md)
  `./gradlew run -Pdriver=com.ultimatelld.problems.elevator.driver.Driver`
- [x] **Problem H** — Expense Sharing (Splitwise)  ·  [docs](docs/problem-h-splitwise.md)
  `./gradlew run -Pdriver=com.ultimatelld.problems.splitwise.driver.Driver`

See [`docs/00-curriculum-outline.md`](docs/00-curriculum-outline.md) for the full map.
