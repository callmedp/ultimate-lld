package com.ultimatelld.problems.ratelimiter.core;

/** Production time source backed by {@link System#nanoTime()} (monotonic). */
public final class SystemTimeSource implements TimeSource {
    @Override
    public long nanoTime() {
        return System.nanoTime();
    }
}
