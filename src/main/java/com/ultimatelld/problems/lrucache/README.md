# LRU Cache — Thread-Safe Bounded Cache

**What this package is:** Interview Question Bank problem F. A fixed-capacity, thread-safe,
generic cache with O(1) `get`/`put` and a **pluggable eviction policy** (LRU, LFU).

Full walkthrough: [`docs/problem-f-lru-cache.md`](../../../../../../../docs/problem-f-lru-cache.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.problems.lrucache.driver.Driver
```

## Layout

| Subpackage | What it does |
|---|---|
| `core` | `Cache` interface + `BoundedCache` — one `ReentrantLock` guards the map and the policy together, so capacity is never exceeded and the two never drift. |
| `eviction` | `EvictionPolicy` (OCP seam) + `LruEvictionPolicy` (LinkedHashSet) and `LfuEvictionPolicy` (frequency buckets in a TreeMap). |
| `driver` | Composition root: shows LRU vs LFU eviction, then a 32-thread stress test proving the capacity invariant. |
