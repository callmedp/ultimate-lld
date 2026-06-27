# Task Scheduler — Task Scheduler / Job Queue

**What this package is:** A job scheduler with worker pools, priority-based and delayed
execution, and a retry policy for failed jobs.

Full walkthrough: [`docs/scenario-d-task-scheduler.md`](../../../../../../../docs/scenario-d-task-scheduler.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.problems.taskscheduler.driver.Driver
```

## Layout

| Subpackage | What it does |
|---|---|
| `core` | `Job` (+ `JobId`, `JobState`, `Priority`), `JobWork` (the unit of work), `RetryPolicy` (backoff/attempt rules). |
| `scheduler` | `JobScheduler` — worker pool that pulls jobs by priority/delay, runs them, and applies the retry policy on failure. |
| `driver` | Composition root: submits jobs of mixed priority/delay and shows ordered, concurrent, retried execution. |
