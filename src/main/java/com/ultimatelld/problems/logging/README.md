# Logging — Logging Framework

**What this package is:** Interview Question Bank problem L. A logging framework with severity levels,
pluggable appenders, and lossless asynchronous delivery.

Full walkthrough: [`docs/problem-l-logging.md`](../../../../../../../docs/problem-l-logging.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.problems.logging.driver.Driver
```

## Layout

| Subpackage | What it does |
|---|---|
| `core` | `LogLevel`, `LogMessage`, `Logger` (level filtering + fan-out, thread-safe config). |
| `appender` | `Appender` (OCP) + `CountingAppender` and `AsyncAppender` (decorator: queue + worker, lossless `close()`). |
| `driver` | Composition root: concurrent logging, level filtering, and zero-loss async flush. |
