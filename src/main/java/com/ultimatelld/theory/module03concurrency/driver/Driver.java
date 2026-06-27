package com.ultimatelld.theory.module03concurrency.driver;

import com.ultimatelld.theory.module03concurrency.inventory.AtomicInventoryService;
import com.ultimatelld.theory.module03concurrency.inventory.BrokenInventoryService;
import com.ultimatelld.theory.module03concurrency.inventory.InventoryService;
import com.ultimatelld.theory.module03concurrency.locking.OptimisticAccount;
import com.ultimatelld.theory.module03concurrency.locking.PessimisticAccount;
import com.ultimatelld.theory.module03concurrency.pool.ObjectPool;
import com.ultimatelld.theory.module03concurrency.pool.PooledConnection;
import com.ultimatelld.theory.module03concurrency.singleton.DoubleCheckedSingleton;
import com.ultimatelld.theory.module03concurrency.singleton.EnumSingleton;
import com.ultimatelld.theory.module03concurrency.singleton.HolderSingleton;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demonstrates the four Module 3 concurrency mechanisms under real thread contention.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        singletonsUnderLoad();
        objectPool();
        optimisticVsPessimistic();
        inventoryRace();
    }

    // ---- 1. Thread-safe singletons: every thread must observe the same instance ----
    private static void singletonsUnderLoad() throws InterruptedException {
        int threads = 64;
        Set<HolderSingleton> holders = ConcurrentHashMap.newKeySet();
        Set<DoubleCheckedSingleton> dcls = ConcurrentHashMap.newKeySet();
        Set<EnumSingleton> enums = ConcurrentHashMap.newKeySet();
        runConcurrently(threads, () -> {
            holders.add(HolderSingleton.getInstance());
            dcls.add(DoubleCheckedSingleton.getInstance());
            enums.add(EnumSingleton.INSTANCE);
        });
        System.out.println("[Singletons] distinct instances across " + threads + " threads -> holder="
                + holders.size() + ", doubleChecked=" + dcls.size() + ", enum=" + enums.size()
                + " (each must be 1)");
    }

    // ---- 2. Object pool: never lends the same resource twice; concurrency capped at pool size ----
    private static void objectPool() throws InterruptedException {
        int poolSize = 3;
        List<PooledConnection> conns = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) conns.add(new PooledConnection(i));
        ObjectPool<PooledConnection> pool = new ObjectPool<>(conns);

        Set<PooledConnection> inUse = ConcurrentHashMap.newKeySet();
        AtomicBoolean doubleCheckout = new AtomicBoolean(false);
        AtomicInteger concurrent = new AtomicInteger();
        AtomicInteger maxConcurrent = new AtomicInteger();
        AtomicInteger served = new AtomicInteger();

        runConcurrently(24, () -> {
            try {
                PooledConnection c = pool.acquire(2, TimeUnit.SECONDS);
                if (c == null) return;
                served.incrementAndGet();
                if (!inUse.add(c)) doubleCheckout.set(true);     // same object handed out twice!
                int now = concurrent.incrementAndGet();
                maxConcurrent.accumulateAndGet(now, Math::max);
                c.execute("SELECT 1");
                Thread.sleep(5);
                concurrent.decrementAndGet();
                inUse.remove(c);
                pool.release(c);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        System.out.println("[ObjectPool] served=" + served.get() + " requests, maxConcurrentInUse="
                + maxConcurrent.get() + " (<= poolSize " + poolSize + "), doubleCheckout="
                + doubleCheckout.get() + " (must be false), availableAtEnd=" + pool.availableCount());
    }

    // ---- 3. Optimistic (CAS+retry) vs pessimistic (lock): both correct; compare retry cost ----
    private static void optimisticVsPessimistic() throws InterruptedException {
        int threads = 16;
        int depositsPerThread = 10_000;
        long expected = (long) threads * depositsPerThread;

        OptimisticAccount optimistic = new OptimisticAccount();
        runConcurrently(threads, () -> {
            for (int i = 0; i < depositsPerThread; i++) optimistic.deposit(1);
        });

        PessimisticAccount pessimistic = new PessimisticAccount();
        runConcurrently(threads, () -> {
            for (int i = 0; i < depositsPerThread; i++) pessimistic.deposit(1);
        });

        System.out.println("[Locking] expected balance=" + expected
                + " | optimistic=" + optimistic.balance() + " (retries=" + optimistic.retries()
                + ", version=" + optimistic.version() + ") | pessimistic=" + pessimistic.balance());
    }

    // ---- 4. Inventory oversell race: broken vs fixed ----
    private static void inventoryRace() throws InterruptedException {
        int stock = 100;
        int buyers = 500;

        InventoryService broken = new BrokenInventoryService(stock);
        AtomicInteger soldBroken = new AtomicInteger();
        runConcurrently(buyers, () -> {
            if (broken.reserve()) soldBroken.incrementAndGet();
        });

        InventoryService fixed = new AtomicInventoryService(stock);
        AtomicInteger soldFixed = new AtomicInteger();
        runConcurrently(buyers, () -> {
            if (fixed.reserve()) soldFixed.incrementAndGet();
        });

        System.out.println("[Inventory] stock=" + stock + ", buyers=" + buyers);
        System.out.println("   " + broken.label() + " -> sold=" + soldBroken.get()
                + ", remaining=" + broken.remaining() + "  (oversell likely; remaining may be < 0)");
        System.out.println("   " + fixed.label() + " -> sold=" + soldFixed.get()
                + ", remaining=" + fixed.remaining() + "  (sold==100, remaining==0, never negative)");
    }

    /** Runs {@code task} on {@code n} threads released simultaneously, and waits for all to finish. */
    private static void runConcurrently(int n, Runnable task) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(64, n));
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(n);
        for (int i = 0; i < n; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    task.run();
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
