# Meeting Scheduler — Room Booking

**What this package is:** Interview Question Bank problem J. Books rooms for time intervals with
capacity constraints and conflict detection, safe under concurrent booking.

Full walkthrough: [`docs/problem-j-meeting-scheduler.md`](../../../../../../../docs/problem-j-meeting-scheduler.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.problems.meetingscheduler.driver.Driver
```

## Layout

| Subpackage | What it does |
|---|---|
| `entity` | `Interval` (half-open, `overlaps`), `MeetingRoom` (per-room lock + TreeMap calendar, atomic `tryBook`), `Booking`. |
| `strategy` | `RoomSelectionStrategy` (OCP) + `SmallestSufficientRoom`. |
| `service` | `MeetingSchedulerService` — filter by capacity, order, atomic book. |
| `exception` | `NoRoomAvailableException`. |
| `driver` | Composition root: 30 concurrent same-slot requests prove no double-booking. |
