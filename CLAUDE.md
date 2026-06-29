# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A teaching codebase, not an application. It is a Java curriculum for SDE-3 Low-Level Design
interview prep. Every leaf package is a **self-contained, runnable sample** with its own
`main`. There is no shared application wiring across packages — each one stands alone and is
meant to be read, run, and studied independently. Each sample has a companion Markdown
walkthrough in `docs/`.

Code is intentionally framework-agnostic: no Spring, no MVC. The value is in the OOD/SOLID/GoF/
concurrency demonstration, so prioritize clarity of design and correctness of thread-safety
over feature breadth when editing or adding samples.

## Commands

```bash
# Build everything (Java 17 toolchain via Gradle wrapper — no local Gradle/JDK install of Gradle needed)
./gradlew build

# Run a specific sample. -Pdriver selects the main class; this is the ONLY way to pick which
# sample runs. With no -Pdriver, it defaults to theory.module01solid's Driver.
./gradlew run -Pdriver=com.ultimatelld.problems.moviebooking.driver.Driver

# Tests: JUnit 5 is configured but there is currently NO src/test — `./gradlew test` is a no-op.
./gradlew test
```

Every runnable sample's main lives at `<package>/driver/Driver.java`. The full list of driver
FQNs is in `README.md` and `docs/00-curriculum-outline.md`.

## Architecture conventions (apply these when adding or editing samples)

The repo enforces a consistent DDD-style layering. Match it exactly when contributing.

- **`entity/`** — rich domain objects: encapsulated state **and** invariant-guarding behavior.
  No anemic getter/setter bags.
- **`service/`** — orchestrators (load → invoke domain method → persist → notify). Design
  patterns and concurrency control live here.
- **`repository/`** — persistence interfaces; in-memory impls use thread-safe collections
  (`ConcurrentHashMap` etc.).
- **`strategy/`** — pluggable algorithms (pricing, allocation, eviction, matching).
- **`driver/`** — the composition root: the *only* place concrete types are wired together,
  and where concurrent clients are simulated (typically `ExecutorService` + `CountDownLatch`).
- **`exception/`** — domain-specific exceptions.

Note: the layer names are not uniform across all samples. Algorithm-centric problems
(`lrucache`, `kvstore`, `ratelimiter`, `taskscheduler`, `urlshortener`, `pubsub`, `logging`)
use a `core/` package (plus things like `eviction/`, `appender/`, `scheduler/`, `broker/`,
`middleware/`) instead of the full entity/service/repository split. Follow the layout of the
sample you are editing rather than forcing every package into the same shape.

Cross-cutting rules baked into every sample, to preserve when editing:
- **Constructor-based dependency injection only** (no field/setter injection, no service locator).
- **Open/Closed**: add a new capability by adding a new class/strategy, not by branching inside
  an existing one.
- **Thread-safety is the point**: all shared mutable state must be guarded — `ReentrantLock`,
  atomics (`AtomicInteger`/`AtomicReference`, CAS), or concurrent collections. Several samples
  inject a manual/controllable clock (e.g. `util/ManualClock`) so time-based logic (TTL, holds,
  expiry) is deterministically testable — prefer this over `System.currentTimeMillis()` calls
  scattered in logic.

## Two sections

- `src/main/java/com/ultimatelld/theory/` — core theory modules (SOLID, GoF patterns,
  concurrency primitives).
- `src/main/java/com/ultimatelld/problems/` — full interview problems (named by problem, e.g.
  `moviebooking`, `parkinglot`, `cabbooking`).
- `src/main/java/com/ultimatelld/common/` — shared value objects (e.g. `Money`).

When adding a new sample, also add its `docs/*.md` walkthrough and link it from
`README.md` and `docs/00-curriculum-outline.md` to keep the curriculum index consistent.