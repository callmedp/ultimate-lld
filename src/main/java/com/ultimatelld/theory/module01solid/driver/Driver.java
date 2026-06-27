package com.ultimatelld.theory.module01solid.driver;

import com.ultimatelld.common.Money;
import com.ultimatelld.theory.module01solid.entity.OrderId;
import com.ultimatelld.theory.module01solid.entity.OrderLine;
import com.ultimatelld.theory.module01solid.exception.IllegalOrderStateException;
import com.ultimatelld.theory.module01solid.repository.InMemoryOrderRepository;
import com.ultimatelld.theory.module01solid.repository.OrderRepository;
import com.ultimatelld.theory.module01solid.service.OrderService;
import com.ultimatelld.theory.module01solid.strategy.ExpressShipping;
import com.ultimatelld.theory.module01solid.strategy.ShippingStrategy;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Composition Root — the ONE place that knows concrete types and wires them together.
 * Also simulates concurrent clients hammering the same order to demonstrate that the
 * service serializes per-order state transitions correctly.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        // --- wiring (dependency injection happens here, nowhere else) ---
        OrderRepository repository = new InMemoryOrderRepository();
        ShippingStrategy shipping = new ExpressShipping();
        OrderService service = new OrderService(repository, shipping);

        // --- happy path ---
        OrderId id = service.placeOrder(List.of(
                new OrderLine("BOOK-001", Money.of(499_00), 2),   // 2 x 499.00
                new OrderLine("PEN-014", Money.of(49_00), 5)       // 5 x 49.00
        ));
        System.out.println("Placed order " + id.value());
        System.out.println("Total with EXPRESS shipping = " + service.totalWithShipping(id));

        service.payOrder(id);
        service.shipOrder(id);
        System.out.println("After pay + ship -> " + repository.findById(id).orElseThrow());

        // --- illegal transition is rejected by the entity ---
        try {
            service.cancelOrder(id); // SHIPPED -> CANCELLED is illegal
        } catch (IllegalOrderStateException e) {
            System.out.println("Correctly rejected: " + e.getMessage());
        }

        // --- concurrency demo: many threads race to cancel the SAME fresh order ---
        OrderId raceId = service.placeOrder(List.of(new OrderLine("SKU-RACE", Money.of(10_00), 1)));
        int threads = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();              // release all threads at once for maximum contention
                    service.cancelOrder(raceId);
                    succeeded.incrementAndGet();
                } catch (IllegalOrderStateException e) {
                    rejected.incrementAndGet();  // already cancelled by a winner
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        //noinspection ResultOfMethodCallIgnored
        pool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);

        System.out.println("Concurrent cancel of one order -> succeeded=" + succeeded.get()
                + ", rejected=" + rejected.get() + " (expected exactly 1 success, 49 rejected)");
        System.out.println("Final state -> " + repository.findById(raceId).orElseThrow());
    }
}
