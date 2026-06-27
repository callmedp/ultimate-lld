# Rate Limiter — Distributed Rate Limiter / Throttling

**What this package is:** A thread-safe rate limiter with pluggable algorithms (Token Bucket,
Leaky Bucket) and a throttling middleware that uses it to admit or reject requests.

Full walkthrough: [`docs/scenario-b-rate-limiter.md`](../../../../../../docs/scenario-b-rate-limiter.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.problems.ratelimiter.driver.Driver
```

## Layout

| Subpackage | What it does |
|---|---|
| `core` | `RateLimiter` interface + `TimeSource` abstraction (`System`/`Manual`) so refill logic is testable without sleeping. |
| `strategy` | `TokenBucketRateLimiter` and `LeakyBucketRateLimiter` — interchangeable algorithms behind the interface. |
| `middleware` | `ThrottlingMiddleware` — wraps a call site, consulting the limiter to allow/deny. |
| `driver` | Composition root: hammers the limiter from many threads to show thread-safe counting and correct admit/reject behavior. |
