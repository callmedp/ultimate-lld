# Parking Lot — High-Throughput Parking Lot

**What this package is:** A parking lot supporting multiple vehicle/spot sizes, pluggable
spot-allocation and fee strategies, and concurrent entry/exit.

Full walkthrough: [`docs/scenario-c-parking-lot.md`](../../../../../../docs/scenario-c-parking-lot.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.problems.parkinglot.driver.Driver
```

## Layout

| Subpackage | What it does |
|---|---|
| `entity` | `ParkingLot`, `ParkingFloor`, `ParkingSpot`, `Vehicle`, `Ticket` + size/type enums and strongly-typed `Ids`. |
| `repository` | `TicketRepository` interface + thread-safe in-memory impl. |
| `strategy` | Spot allocation (`BestFit`, `NearestFirst` behind `SpotAllocationStrategy`) and `FeeStrategy` (`HourlyFeeStrategy`) — both pluggable (OCP). |
| `service` | `ParkingLotService` orchestrates park/unpark with concurrency control. |
| `util` | `Clock` abstraction so fee-by-duration is testable. |
| `exception` | `ParkingFullException`. |
| `driver` | Composition root: concurrent entry/exit to prove no double-allocation of a spot. |
