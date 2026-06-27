package com.ultimatelld.problems.taskscheduler.core;

/**
 * Retry policy: total number of attempts before a job is sent to the dead-letter list.
 * {@code maxAttempts == 1} means no retries.
 */
public record RetryPolicy(int maxAttempts) {

    public RetryPolicy {
        if (maxAttempts < 1) throw new IllegalArgumentException("maxAttempts must be >= 1");
    }

    public static RetryPolicy noRetry() {
        return new RetryPolicy(1);
    }

    public static RetryPolicy ofAttempts(int attempts) {
        return new RetryPolicy(attempts);
    }
}
