# Cab Booking — Ride Matching

**What this package is:** Interview Question Bank problem M. Matches riders to nearby available
drivers under concurrency, with pluggable matching and fare policies.

Full walkthrough: [`docs/problem-m-cab-booking.md`](../../../../../../../docs/problem-m-cab-booking.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.problems.cabbooking.driver.Driver
```

## Layout

| Subpackage | What it does |
|---|---|
| `entity` | `CabDriver` (atomic `tryReserve` CAS — no double-booking), `Location`, `Rider`, `Trip`, `DriverStatus`. |
| `strategy` | `DriverMatchingStrategy` (OCP) + `NearestDriverStrategy`; `FareStrategy` + `DistanceFareStrategy`. |
| `service` | `RideService` — rank candidates, atomically reserve, quote fare, end trip. |
| `exception` | `NoDriverAvailableException`. |
| `driver` | Composition root: 20 riders vs 5 drivers prove at-most-one-rider-per-driver. |
