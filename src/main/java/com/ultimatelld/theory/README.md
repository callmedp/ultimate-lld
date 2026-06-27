# Theory — Core LLD Foundations

**What this package group is:** Section 1 of the curriculum — the reusable theory. These are the
principles, patterns, and concurrency building blocks that every `problems/` scenario draws on.

| Package | Module | What it covers | Docs |
|---|---|---|---|
| `module01solid` | **1 — Advanced OOP & SOLID** | Rich vs. anemic domain models; the Entity/Repository/Service/Driver layering skeleton; SOLID violations and their fixes. | [docs/module-01-solid.md](../../../../../../docs/module-01-solid.md) |
| `module02patterns` | **2 — Core Design Patterns** | The 8 core GoF patterns: Factory, Builder, Singleton, Decorator, Facade, Strategy, Observer, State. | [docs/module-02-patterns.md](../../../../../../docs/module-02-patterns.md) |
| `module03concurrency` | **3 — Concurrency & State** | Thread-safe singletons, object pools, optimistic vs. pessimistic locking, a broken-vs-correct race-condition demo. | [docs/module-03-concurrency.md](../../../../../../docs/module-03-concurrency.md) |

Each module ships a `driver/Driver` you can run:
```bash
./gradlew run -Pdriver=com.ultimatelld.theory.module02patterns.driver.Driver
```

See the matching `README.md` inside each module folder for its subpackage map.
