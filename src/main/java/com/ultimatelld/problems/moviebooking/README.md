# Movie Booking — Concurrent Movie Ticket Booking

**What this package is:** A movie-ticket booking system whose hard part is **exact-moment seat
locking** under concurrency — two users must never book the same seat, holds must expire, and
payment failures must release the seat.

Full walkthrough: [`docs/scenario-a-movie-booking.md`](../../../../../../docs/scenario-a-movie-booking.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.problems.moviebooking.driver.Driver
```

## Layout

| Subpackage | What it does |
|---|---|
| `entity` | `Show`, `Seat`, `SeatHold`, `Booking` and their status/tier enums + strongly-typed `Ids`. The seat owns its hold/booked state. |
| `repository` | `ShowRepository` / `BookingRepository` interfaces + thread-safe in-memory impls. |
| `service` | `BookingService` orchestrates hold → pay → confirm with locking; `PaymentGateway` abstraction. |
| `strategy` | `SeatPricing` (e.g. tier-based) — pluggable pricing. |
| `util` | `Clock` abstraction (`System`/`Manual`) so hold-expiry is testable without real time. |
| `exception` | `SeatUnavailableException`, `HoldExpiredException`, `PaymentDeclinedException`. |
| `driver` | Composition root: races many threads at the same seat to prove exactly one booking wins. |
