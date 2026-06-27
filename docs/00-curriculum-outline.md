# Ultimate LLD — Curriculum Outline

A self-paced, production-grade curriculum for Low-Level Design in Java, targeted at SDE-3
interviews. Framework-agnostic — pure OOD, SOLID, GoF patterns, and DDD.

## Section 1 — Core LLD Theory & Architectural Foundations

| Module | Focus | Key Deliverables | Code package |
|--------|-------|------------------|--------------|
| **1. Advanced OOP & SOLID** | LLD-specific violations + remedies per principle; rich vs. anemic domain models | Per-principle "smell → fix" pairs, encapsulation deep-dive, layering skeleton | `module01solid` |
| **2. Core Design Patterns** | Creational (Factory, Builder, thread-safe Singleton), Structural (Decorator, Facade), Behavioral (Strategy, Observer, State) | One canonical, framework-agnostic implementation per pattern | `module02patterns` |
| **3. SDE-3 Concurrency & State** | Thread-safe singletons, object pools, optimistic vs. pessimistic locking in-memory, race conditions in booking/inventory | Lock strategy decision matrix, CAS vs. lock examples | `module03concurrency` |

## Section 2 — SDE-3 Elite Interview Question Bank

Each problem follows a 5-part rubric:
1. Problem Statement & SDE-3 Scale/Constraints (concurrency + extensibility focus)
2. Use Cases & Clarifying Questions (scoping questions a candidate should ask)
3. Class Diagram & Interactivity (Mermaid: Entities, Services, Repositories)
4. Core Production Skeleton Code (fully implemented — no stub comments in core logic)
5. Edge Case Analysis (race conditions, fault isolation, partial failures)

| Scenario | Problem | Concurrency emphasis |
|----------|---------|----------------------|
| **A** | Concurrent Movie Ticket Booking | Exact-moment seat locking, booking timeouts, race conditions |
| **B** | Distributed Rate Limiter / Throttling | Token Bucket / Leaky Bucket, thread-safe counters |
| **C** | High-Throughput Parking Lot | Vehicle types, pluggable allocation strategies, concurrent entry/exit |
| **D** | Task Scheduler / Job Queue | Worker pools, delayed execution, priority-based execution |
| **E** | In-Memory Pub-Sub Messaging | Topic partitions, consumer-group offsets, delivery guarantees |

## How to navigate

- Theory and walkthroughs live in `docs/`, one Markdown file per module/scenario.
- Runnable code lives under `src/main/java/com/ultimatelld/`, one package per module/scenario.
- Every package ships a `driver/Driver.java` you can run with
  `./gradlew run -Pdriver=<fully.qualified.Driver>`.
