package com.ultimatelld.problems.ratelimiter.strategy;

import com.ultimatelld.problems.ratelimiter.core.RateLimiter;
import com.ultimatelld.problems.ratelimiter.core.TimeSource;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Token Bucket: each client holds up to {@code capacity} tokens, refilled continuously at
 * {@code refillPerSecond}. A request consumes one token; if none remain, it is throttled.
 * <p>
 * Refill is computed LAZILY from elapsed time on each call — no background refill thread needed.
 * Each client's bucket is guarded by its own {@link ReentrantLock}, so different clients never
 * contend, while concurrent requests for the SAME client are serialized (no token double-spend).
 */
public final class TokenBucketRateLimiter implements RateLimiter {

    private final double capacity;
    private final double refillTokensPerNano;
    private final TimeSource time;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(long capacity, double refillPerSecond, TimeSource time) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        if (refillPerSecond <= 0) throw new IllegalArgumentException("refillPerSecond must be > 0");
        this.capacity = capacity;
        this.refillTokensPerNano = refillPerSecond / 1_000_000_000.0;
        this.time = Objects.requireNonNull(time);
    }

    @Override
    public boolean allow(String clientId) {
        Bucket bucket = buckets.computeIfAbsent(clientId, k -> new Bucket(capacity, time.nanoTime()));
        bucket.lock.lock();
        try {
            refill(bucket);
            if (bucket.tokens >= 1.0) {
                bucket.tokens -= 1.0;
                return true;
            }
            return false;
        } finally {
            bucket.lock.unlock();
        }
    }

    private void refill(Bucket bucket) {
        long now = time.nanoTime();
        long elapsed = now - bucket.lastRefillNanos;
        if (elapsed > 0) {
            bucket.tokens = Math.min(capacity, bucket.tokens + elapsed * refillTokensPerNano);
            bucket.lastRefillNanos = now;
        }
    }

    @Override
    public String algorithm() {
        return "TOKEN_BUCKET";
    }

    private static final class Bucket {
        final ReentrantLock lock = new ReentrantLock();
        double tokens;
        long lastRefillNanos;

        Bucket(double tokens, long lastRefillNanos) {
            this.tokens = tokens;
            this.lastRefillNanos = lastRefillNanos;
        }
    }
}
