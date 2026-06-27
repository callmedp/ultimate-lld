package com.ultimatelld.problems.taskscheduler.core;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A schedulable unit of work. Immutable in its definition (id, name, priority, work, retry policy);
 * its runtime fields (attempt count, state) are mutated only by the scheduler's workers via
 * thread-safe atomics. The {@code seq} field provides a stable FIFO tiebreak for equal priorities.
 */
public final class Job {

    private final JobId id;
    private final String name;
    private final Priority priority;
    private final JobWork work;
    private final RetryPolicy retryPolicy;
    private final long seq;

    private final AtomicInteger attempts = new AtomicInteger(0);
    private final AtomicReference<JobState> state = new AtomicReference<>(JobState.PENDING);

    public Job(JobId id, String name, Priority priority, JobWork work, RetryPolicy retryPolicy, long seq) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.priority = Objects.requireNonNull(priority, "priority");
        this.work = Objects.requireNonNull(work, "work");
        this.retryPolicy = Objects.requireNonNull(retryPolicy, "retryPolicy");
        this.seq = seq;
    }

    public JobId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Priority priority() {
        return priority;
    }

    public JobWork work() {
        return work;
    }

    public RetryPolicy retryPolicy() {
        return retryPolicy;
    }

    public long seq() {
        return seq;
    }

    public int incrementAttempt() {
        return attempts.incrementAndGet();
    }

    public int attempts() {
        return attempts.get();
    }

    public JobState state() {
        return state.get();
    }

    public void markRunning() {
        state.set(JobState.RUNNING);
    }

    public void markPendingForRetry() {
        state.set(JobState.PENDING);
    }

    public void markSucceeded() {
        state.set(JobState.SUCCEEDED);
    }

    public void markDead() {
        state.set(JobState.DEAD);
    }

    @Override
    public String toString() {
        return name + "(" + id + ", " + priority + ")";
    }
}
