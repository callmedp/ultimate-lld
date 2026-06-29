package com.ultimatelld.problems.kvstore.driver;

import com.ultimatelld.problems.kvstore.core.Clock;
import com.ultimatelld.problems.kvstore.core.InMemoryKeyValueStore;
import com.ultimatelld.problems.kvstore.core.KeyValueStore;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Composition root + TTL and concurrency demo for the KV store.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        Clock.Manual clock = new Clock.Manual(0L);
        KeyValueStore<String, String> store = new InMemoryKeyValueStore<>(clock);

        // ---- 1. TTL expiry (lazy + active) ----
        store.put("session:1", "alice", 100);   // expires at t=100
        store.put("config", "v1");                // no expiry
        System.out.println("[TTL] @t=0   get session:1 = " + store.get("session:1").orElse("-")
                + ", config = " + store.get("config").orElse("-"));
        clock.advanceMillis(101);                 // now past the TTL
        System.out.println("[TTL] @t=101 get session:1 = " + store.get("session:1").orElse("(expired)")
                + " (lazy expiry), config = " + store.get("config").orElse("-"));

        store.put("a", "1", 50);
        store.put("b", "2", 50);
        clock.advanceMillis(60);
        long purged = store.purgeExpired();        // active sweep for keys never read again
        System.out.println("[TTL] purgeExpired removed " + purged + " entries; size now = " + store.size());

        // ---- 2. Concurrency: mixed put/get/delete + TTL, no corruption ----
        KeyValueStore<Integer, Integer> kv = new InMemoryKeyValueStore<>(new Clock.System());
        int threads = 32, opsPerThread = 30_000, keySpace = 500;
        AtomicBoolean error = new AtomicBoolean(false);
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
                        switch (rnd.nextInt(4)) {
                            case 0 -> kv.put(key, i);
                            case 1 -> kv.put(key, i, 5);   // short TTL
                            case 2 -> kv.get(key);
                            default -> kv.delete(key);
                        }
                    }
                } catch (Exception e) {
                    error.set(true);
                } catch (Throwable t2) {
                    error.set(true);
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        pool.shutdownNow();
        System.out.println("[Concurrency] " + (threads * opsPerThread) + " mixed ops -> errors="
                + error.get() + " (must be false), finalSize=" + kv.size()
                + " (<= keySpace " + keySpace + ")");
    }
}
