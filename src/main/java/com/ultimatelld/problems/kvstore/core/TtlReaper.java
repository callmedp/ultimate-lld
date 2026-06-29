package com.ultimatelld.problems.kvstore.core;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Background sweeper that periodically calls {@link KeyValueStore#purgeExpired()} so entries that
 * are never read again are still reclaimed. A daemon single-thread scheduler; {@link #close()}
 * stops it cleanly.
 */
public final class TtlReaper implements AutoCloseable {

    private final KeyValueStore<?, ?> store;
    private final ScheduledExecutorService scheduler;
    private final AtomicLong totalPurged = new AtomicLong();

    public TtlReaper(KeyValueStore<?, ?> store, long periodMillis) {
        this.store = Objects.requireNonNull(store);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "kv-ttl-reaper");
            t.setDaemon(true);
            return t;
        });
        this.scheduler.scheduleAtFixedRate(
                () -> totalPurged.addAndGet(store.purgeExpired()),
                periodMillis, periodMillis, TimeUnit.MILLISECONDS);
    }

    public long totalPurged() {
        return totalPurged.get();
    }

    @Override
    public void close() {
        scheduler.shutdownNow();
    }
}
