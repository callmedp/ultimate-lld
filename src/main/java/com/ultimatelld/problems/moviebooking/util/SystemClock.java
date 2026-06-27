package com.ultimatelld.problems.moviebooking.util;

/** Wall-clock implementation for production. */
public final class SystemClock implements Clock {
    @Override
    public long nowMillis() {
        return System.currentTimeMillis();
    }
}
