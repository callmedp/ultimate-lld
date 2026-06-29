package com.ultimatelld.problems.kvstore.core;

import java.util.concurrent.atomic.AtomicLong;

/** Injectable clock so TTL expiry is deterministic in tests/demos. */
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
