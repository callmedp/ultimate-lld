package com.ultimatelld.problems.taskscheduler.core;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/** Strongly-typed, monotonically increasing job identifier. */
public record JobId(long value) {

    private static final AtomicLong SEQ = new AtomicLong();

    public JobId {
        if (value < 0) throw new IllegalArgumentException("JobId must be >= 0");
    }

    public static JobId next() {
        return new JobId(SEQ.incrementAndGet());
    }

    @Override
    public String toString() {
        return "job-" + value;
    }

    public boolean equalsId(JobId other) {
        return Objects.equals(this, other);
    }
}
