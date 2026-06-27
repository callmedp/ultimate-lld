package com.ultimatelld.problems.lrucache.driver;

import com.ultimatelld.problems.lrucache.core.BoundedCache;
import com.ultimatelld.problems.lrucache.eviction.LfuEvictionPolicy;
import com.ultimatelld.problems.lrucache.eviction.LruEvictionPolicy;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Composition root + concurrency stress test for the bounded cache.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        // ---- 1. LRU semantics ----
        BoundedCache<Integer, String> lru = new BoundedCache<>(3, new LruEvictionPolicy<>());
        lru.put(1, "a");
        lru.put(2, "b");
        lru.put(3, "c");
        lru.get(1);          // touch 1 -> now 2 is the LRU
        lru.put(4, "d");     // evicts 2
        System.out.println("[LRU cap=3] after put1,2,3 / get1 / put4 -> "
                + "1=" + lru.get(1).orElse("-") + ", 2(evicted)=" + lru.get(2).orElse("-")
                + ", 3=" + lru.get(3).orElse("-") + ", 4=" + lru.get(4).orElse("-"));

        // ---- 2. LFU semantics (same cache code, different policy) ----
        BoundedCache<Integer, String> lfu = new BoundedCache<>(3, new LfuEvictionPolicy<>());
        lfu.put(1, "a");
        lfu.put(2, "b");
        lfu.put(3, "c");
        lfu.get(1); lfu.get(1);   // freq(1)=3
        lfu.get(2);                // freq(2)=2
        lfu.put(4, "d");           // evicts 3 (freq 1, the least frequently used)
        System.out.println("[LFU cap=3] freqs 1>2>3, then put4 -> "
                + "3(evicted)=" + lfu.get(3).orElse("-") + ", 1=" + lfu.get(1).orElse("-")
                + ", 4=" + lfu.get(4).orElse("-"));

        // ---- 3. Concurrency stress: capacity invariant must always hold ----
        int capacity = 50;
        BoundedCache<Integer, Integer> cache = new BoundedCache<>(capacity, new LruEvictionPolicy<>());
        int threads = 32;
        int opsPerThread = 20_000;
        int keySpace = 200;
        AtomicBoolean overCapacity = new AtomicBoolean(false);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    ThreadLocalRandom rnd = ThreadLocalRandom.current();
                    for (int i = 0; i < opsPerThread; i++) {
                        int key = rnd.nextInt(keySpace);
                        if (rnd.nextBoolean()) cache.put(key, key);
                        else cache.get(key);
                        if (cache.size() > capacity) overCapacity.set(true);
                    }
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

        System.out.println("[Concurrency] " + threads + " threads x " + opsPerThread + " ops -> "
                + "everExceededCapacity=" + overCapacity.get() + " (must be false), "
                + "consistent(map==policy)=" + cache.isConsistent()
                + ", finalSize=" + cache.size() + " (<= " + capacity + ")");
        System.out.println("   stats: hits=" + cache.hits() + ", misses=" + cache.misses()
                + ", evictions=" + cache.evictions());
    }
}
