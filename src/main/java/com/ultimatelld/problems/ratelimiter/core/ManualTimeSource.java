package com.ultimatelld.problems.ratelimiter.core;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;

/**
 * Test/demo time source whose clock only moves when {@link #advance} is called.
 * Thread-safe via {@link AtomicLong} so concurrent readers see a consistent value.
 */
public final class ManualTimeSource implements TimeSource {

    private final AtomicLong nanos = new AtomicLong(0);

    @Override
    public long nanoTime() {
        return nanos.get();
    }

    public void advance(long amount, TimeUnit unit) {
        nanos.addAndGet(unit.toNanos(amount));
    }
}
