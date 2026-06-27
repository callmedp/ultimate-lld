package com.ultimatelld.problems.moviebooking.util;

import java.util.concurrent.atomic.AtomicLong;

/** Deterministic clock that only advances when told to — drives the hold-expiry demo. */
public final class ManualClock implements Clock {
    private final AtomicLong millis;

    public ManualClock(long startMillis) {
        this.millis = new AtomicLong(startMillis);
    }

    @Override
    public long nowMillis() {
        return millis.get();
    }

    public void advanceMillis(long delta) {
        millis.addAndGet(delta);
    }
}
