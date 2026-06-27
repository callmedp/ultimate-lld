package com.ultimatelld.problems.ratelimiter.driver;

import com.ultimatelld.problems.ratelimiter.core.ManualTimeSource;
import com.ultimatelld.problems.ratelimiter.core.RateLimiter;
import com.ultimatelld.problems.ratelimiter.middleware.ThrottlingMiddleware;
import com.ultimatelld.problems.ratelimiter.strategy.TokenBucketRateLimiter;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Composition root + concurrent client simulation for the rate limiter.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        ManualTimeSource clock = new ManualTimeSource();
        // capacity 10 tokens, refilling 5 tokens/second.
        RateLimiter limiter = new TokenBucketRateLimiter(10, 5.0, clock);

        // --- 1. Instantaneous burst from one client: exactly capacity should pass ---
        int burst = 50;
        AtomicInteger allowed = new AtomicInteger();
        AtomicInteger throttled = new AtomicInteger();
        runBurst(limiter, "clientA", burst, allowed, throttled);
        System.out.println("Burst of " + burst + " for clientA @t=0 -> allowed=" + allowed.get()
                + ", throttled=" + throttled.get() + " (expected allowed=10)");

        // --- 2. Advance the clock 1 second -> 5 tokens refilled ---
        clock.advance(1, TimeUnit.SECONDS);
        allowed.set(0);
        throttled.set(0);
        runBurst(limiter, "clientA", burst, allowed, throttled);
        System.out.println("Burst of " + burst + " for clientA @t=1s -> allowed=" + allowed.get()
                + ", throttled=" + throttled.get() + " (expected allowed=5 from refill)");

        // --- 3. Per-client isolation: a fresh client has its own full bucket ---
        int allowedB = 0;
        for (int i = 0; i < 12; i++) {
            if (limiter.allow("clientB")) allowedB++;
        }
        System.out.println("clientB (independent bucket) allowed=" + allowedB + " of 12 (expected 10)");

        // --- 4. Middleware facade in action ---
        ThrottlingMiddleware middleware = new ThrottlingMiddleware(limiter);
        ThrottlingMiddleware.Response<String> r = middleware.handle("clientC", () -> "payload-ok");
        System.out.println("Middleware clientC first call -> status=" + r.status() + ", body=" + r.body());
    }

    private static void runBurst(RateLimiter limiter, String client, int n,
                                 AtomicInteger allowed, AtomicInteger throttled) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(32, n));
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(n);
        for (int i = 0; i < n; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    if (limiter.allow(client)) allowed.incrementAndGet();
                    else throttled.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        pool.shutdownNow();
    }
}
