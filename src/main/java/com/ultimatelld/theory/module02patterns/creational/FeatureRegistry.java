package com.ultimatelld.theory.module02patterns.creational;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SINGLETON — a real, THREAD-SAFE registry/config that the driver hits from many threads.
 * <p>
 * Built with the initialization-on-demand holder idiom (recommended). Its shared mutable state
 * is a {@link ConcurrentHashMap} of feature flags plus an {@link AtomicLong} read counter, so
 * concurrent reads and writes are safe without coarse external locking. This is the realistic
 * shape of a singleton in production: not a static bag of getters, but a guarded shared service.
 */
public final class FeatureRegistry {

    private final ConcurrentHashMap<String, Boolean> flags = new ConcurrentHashMap<>();
    private final AtomicLong reads = new AtomicLong();

    private FeatureRegistry() {
    }

    private static final class Holder {
        private static final FeatureRegistry INSTANCE = new FeatureRegistry();
    }

    public static FeatureRegistry getInstance() {
        return Holder.INSTANCE;
    }

    /** Atomic set; returns the previous value (null if unset). */
    public Boolean enable(String feature, boolean enabled) {
        Objects.requireNonNull(feature, "feature");
        return flags.put(feature, enabled);
    }

    /** Lock-free read; counts every lookup so the driver can assert all reads landed. */
    public boolean isEnabled(String feature) {
        Objects.requireNonNull(feature, "feature");
        reads.incrementAndGet();
        return flags.getOrDefault(feature, false);
    }

    public long readCount() {
        return reads.get();
    }

    /** Read-only snapshot of the current flags. */
    public Map<String, Boolean> snapshot() {
        return Map.copyOf(flags);
    }
}
