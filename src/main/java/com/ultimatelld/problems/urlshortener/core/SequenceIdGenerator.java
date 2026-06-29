package com.ultimatelld.problems.urlshortener.core;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Monotonic sequence encoded to base-62. The {@link AtomicLong} makes {@code next()} thread-safe and
 * collision-free: every caller gets a distinct number, hence a distinct short code, with no locking.
 * (A distributed deployment would hand each node a pre-allocated id range from a central counter.)
 */
public final class SequenceIdGenerator implements IdGenerator {

    private final AtomicLong seq;

    public SequenceIdGenerator(long start) {
        this.seq = new AtomicLong(start);
    }

    @Override
    public String next() {
        return Base62.encode(seq.incrementAndGet());
    }
}
