package com.ultimatelld.theory.module03concurrency.singleton;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Initialization-on-demand holder idiom — the preferred lazy singleton in Java.
 * The nested {@code Holder} class is not loaded until {@link #getInstance()} is first called, and
 * the JVM guarantees class initialization is thread-safe. No synchronization, no volatile, no cost.
 */
public final class HolderSingleton {

    private final AtomicLong hits = new AtomicLong();

    private HolderSingleton() {
    }

    private static final class Holder {
        private static final HolderSingleton INSTANCE = new HolderSingleton();
    }

    public static HolderSingleton getInstance() {
        return Holder.INSTANCE;
    }

    public long recordHit() {
        return hits.incrementAndGet();
    }

    public long hits() {
        return hits.get();
    }
}
