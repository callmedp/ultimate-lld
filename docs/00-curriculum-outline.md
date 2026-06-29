# Ultimate LLD — Curriculum Outline

A self-paced, production-grade curriculum for Low-Level Design in Java, targeted at SDE-3
interviews. Framework-agnostic — pure OOD, SOLID, GoF patterns, and DDD.

## Section 1 — Core LLD Theory & Architectural Foundations

| Module | Focus | Key Deliverables | Code package |
|--------|-------|------------------|--------------|
| **1. Advanced OOP & SOLID** | LLD-specific violations + remedies per principle; rich vs. anemic domain models | Per-principle "smell → fix" pairs, encapsulation deep-dive, layering skeleton | `theory.module01solid` |
| **2. Core Design Patterns** | Creational (Factory, Builder, thread-safe Singleton), Structural (Decorator, Facade), Behavioral (Strategy, Observer, State) | One canonical, framework-agnostic implementation per pattern | `theory.module02patterns` |
| **3. SDE-3 Concurrency & State** | Thread-safe singletons, object pools, optimistic vs. pessimistic locking in-memory, race conditions in booking/inventory | Lock strategy decision matrix, CAS vs. lock examples | `theory.module03concurrency` |

## Section 2 — SDE-3 Elite Interview Question Bank

Each problem follows a 5-part rubric:
1. Problem Statement & SDE-3 Scale/Constraints (concurrency + extensibility focus)
2. Use Cases & Clarifying Questions (scoping questions a candidate should ask)
3. Class Diagram & Interactivity (Mermaid: Entities, Services, Repositories)
4. Core Production Skeleton Code (fully implemented — no stub comments in core logic)
5. Edge Case Analysis (race conditions, fault isolation, partial failures)

| # | Problem | Concurrency emphasis | Code package |
|---|---------|----------------------|--------------|
| **A** | Concurrent Movie Ticket Booking | Exact-moment seat locking, booking timeouts, race conditions | `problems.moviebooking` |
| **B** | Distributed Rate Limiter / Throttling | Token Bucket / Leaky Bucket, thread-safe counters | `problems.ratelimiter` |
| **C** | High-Throughput Parking Lot | Vehicle types, pluggable allocation strategies, concurrent entry/exit | `problems.parkinglot` |
| **D** | Task Scheduler / Job Queue | Worker pools, delayed execution, priority-based execution | `problems.taskscheduler` |
| **E** | In-Memory Pub-Sub Messaging | Topic partitions, consumer-group offsets, delivery guarantees | `problems.pubsub` |
| **F** | Thread-Safe LRU/LFU Cache | O(1) get/put, single-lock atomicity, pluggable eviction (OCP) | `problems.lrucache` |
| **G** | Multi-Car Elevator System | Per-car locks, LOOK algorithm, pluggable dispatch | `problems.elevator` |
| **H** | Expense Sharing (Splitwise) | Split strategies, thread-safe ledger, min-cash-flow settlement | `problems.splitwise` |
| **I** | Key-Value Store with TTL (mini-Redis) | Concurrent get/put, lazy + active expiry, background reaper | `problems.kvstore` |
| **J** | Meeting Room Scheduler | Interval conflict detection, atomic per-room booking | `problems.meetingscheduler` |
| **K** | URL Shortener (TinyURL) | Base-62 ids, idempotent + collision-free shortening | `problems.urlshortener` |
| **L** | Logging Framework | Levels, pluggable appenders, lossless async delivery | `problems.logging` |
| **M** | Cab Booking (Ride Matching) | Lock-free driver assignment (CAS), pluggable matching/fare | `problems.cabbooking` |

## How to navigate

- Theory and walkthroughs live in `docs/`, one Markdown file per module/problem.
- Runnable code lives under `src/main/java/com/ultimatelld/`, split into two groups:
  **`theory.*`** (Section 1 modules) and **`problems.*`** (Section 2 question bank).
- Every package ships a `driver/Driver.java` you can run with
  `./gradlew run -Pdriver=<fully.qualified.Driver>`, e.g.
  `./gradlew run -Pdriver=com.ultimatelld.problems.moviebooking.driver.Driver`.
