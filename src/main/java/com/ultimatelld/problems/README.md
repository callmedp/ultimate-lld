# Problems — SDE-3 Elite Interview Question Bank

**What this package group is:** Section 2 of the curriculum — full LLD solutions to the classic
SDE-3 interview problems. Each applies the `theory/` foundations (rich entities, layering,
strategies, thread-safety) to a realistic, concurrency-heavy problem.

Each folder is named after the problem it solves:

| Package | Problem | Concurrency emphasis | Docs |
|---|---|---|---|
| `moviebooking` | **Concurrent Movie Ticket Booking** | Exact-moment seat locking, hold expiry, payment-failure release. | [docs/scenario-a-movie-booking.md](../../../../../../docs/scenario-a-movie-booking.md) |
| `ratelimiter` | **Distributed Rate Limiter** | Token/Leaky Bucket, thread-safe counters, throttling middleware. | [docs/scenario-b-rate-limiter.md](../../../../../../docs/scenario-b-rate-limiter.md) |
| `parkinglot` | **High-Throughput Parking Lot** | Pluggable allocation/fee strategies, concurrent entry/exit. | [docs/scenario-c-parking-lot.md](../../../../../../docs/scenario-c-parking-lot.md) |
| `taskscheduler` | **Task Scheduler / Job Queue** | Worker pools, priority + delayed execution, retry policy. | [docs/scenario-d-task-scheduler.md](../../../../../../docs/scenario-d-task-scheduler.md) |
| `pubsub` | **In-Memory Pub-Sub Messaging** | Topic partitions, consumer-group offsets, delivery guarantees. | [docs/scenario-e-pubsub.md](../../../../../../docs/scenario-e-pubsub.md) |

Each problem ships a `driver/Driver` you can run:
```bash
./gradlew run -Pdriver=com.ultimatelld.problems.moviebooking.driver.Driver
```

See the matching `README.md` inside each problem folder for its subpackage map.
