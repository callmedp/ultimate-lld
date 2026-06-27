package com.ultimatelld.problems.parkinglot.util;

import java.util.concurrent.atomic.AtomicLong;

/** Injectable clock. The nested ManualClock drives deterministic fee calculations in the demo. */
public interface Clock {
    long nowMillis();

    final class System implements Clock {
        @Override
        public long nowMillis() {
            return java.lang.System.currentTimeMillis();
        }
    }

    final class Manual implements Clock {
        private final AtomicLong millis;

        public Manual(long start) {
            this.millis = new AtomicLong(start);
        }

        @Override
        public long nowMillis() {
            return millis.get();
        }

        public void advanceMillis(long delta) {
            millis.addAndGet(delta);
        }
    }
}
