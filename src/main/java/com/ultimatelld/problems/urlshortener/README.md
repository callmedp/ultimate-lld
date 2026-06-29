# URL Shortener — TinyURL

**What this package is:** Interview Question Bank problem K. Generates collision-free short codes for
long URLs (base-62), with idempotent shortening and custom aliases.

Full walkthrough: [`docs/problem-k-url-shortener.md`](../../../../../../../docs/problem-k-url-shortener.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.problems.urlshortener.driver.Driver
```

## Layout

| Subpackage | What it does |
|---|---|
| `core` | `Base62` encoder, `IdGenerator` (OCP) + `SequenceIdGenerator` (AtomicLong, collision-free). |
| `service` | `UrlRepository` (atomic `computeIfAbsent` for idempotency, `putIfAbsent` for aliases) + `UrlShortenerService`. |
| `exception` | `AliasAlreadyExistsException`. |
| `driver` | Composition root: idempotency + custom alias + 64k-call concurrency check. |
