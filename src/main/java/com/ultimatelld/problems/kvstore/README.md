# KV Store — In-Memory Key-Value Store with TTL

**What this package is:** Interview Question Bank problem I. A concurrent in-memory key-value store
(mini-Redis) with per-key TTL via lazy + active expiry.

Full walkthrough: [`docs/problem-i-kv-store.md`](../../../../../../../docs/problem-i-kv-store.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.problems.kvstore.driver.Driver
```

## Layout

| Subpackage | What it does |
|---|---|
| `core` | `KeyValueStore` + `InMemoryKeyValueStore` (ConcurrentHashMap, conditional remove), `Clock` (System/Manual), `TtlReaper` (background sweeper). |
| `driver` | Composition root: TTL expiry demo + 960k-op concurrency stress test. |
