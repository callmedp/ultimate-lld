package com.ultimatelld.problems.ratelimiter.strategy;

import com.ultimatelld.problems.ratelimiter.core.RateLimiter;
import com.ultimatelld.problems.ratelimiter.core.TimeSource;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Leaky Bucket: requests fill a bucket that "leaks" at a fixed rate. A request is admitted only
 * if there is room (level &lt; capacity) after accounting for what has leaked since the last call.
 * Smooths bursts into a steady outflow — the dual of token bucket.
 */
public final class LeakyBucketRateLimiter implements RateLimiter {

    private final double capacity;
    private final double leakPerNano;
    private final TimeSource time;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public LeakyBucketRateLimiter(long capacity, double leakPerSecond, TimeSource time) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        if (leakPerSecond <= 0) throw new IllegalArgumentException("leakPerSecond must be > 0");
        this.capacity = capacity;
        this.leakPerNano = leakPerSecond / 1_000_000_000.0;
        this.time = Objects.requireNonNull(time);
    }

    @Override
    public boolean allow(String clientId) {
        Bucket bucket = buckets.computeIfAbsent(clientId, k -> new Bucket(time.nanoTime()));
        bucket.lock.lock();
        try {
            leak(bucket);
            if (bucket.level + 1.0 <= capacity) {
                bucket.level += 1.0;
                return true;
            }
            return false;
        } finally {
            bucket.lock.unlock();
        }
    }

    private void leak(Bucket bucket) {
        long now = time.nanoTime();
        long elapsed = now - bucket.lastLeakNanos;
        if (elapsed > 0) {
            bucket.level = Math.max(0.0, bucket.level - elapsed * leakPerNano);
            bucket.lastLeakNanos = now;
        }
    }

    @Override
    public String algorithm() {
        return "LEAKY_BUCKET";
    }

    private static final class Bucket {
        final ReentrantLock lock = new ReentrantLock();
        double level;
        long lastLeakNanos;

        Bucket(long lastLeakNanos) {
            this.lastLeakNanos = lastLeakNanos;
        }
    }
}
