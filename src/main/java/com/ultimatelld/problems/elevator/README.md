# Elevator — Multi-Car Elevator System

**What this package is:** Interview Question Bank problem G. A bank of elevators serving concurrent
hall calls, with a pluggable dispatch policy and the LOOK movement algorithm per car.

Full walkthrough: [`docs/problem-g-elevator.md`](../../../../../../../docs/problem-g-elevator.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.problems.elevator.driver.Driver
```

## Layout

| Subpackage | What it does |
|---|---|
| `entity` | `Elevator` (per-car `ReentrantLock`, LOOK algorithm over a `NavigableSet` of stops), `Request`, `Direction`. |
| `strategy` | `DispatchStrategy` (OCP) + `NearestCarStrategy` and `LeastBusyStrategy`. |
| `service` | `ElevatorController` — thread-safe request submission + a stepped simulation of all cars. |
| `driver` | Composition root: 40 concurrent hall calls, then runs the simulation to completion and verifies every floor was served. |
