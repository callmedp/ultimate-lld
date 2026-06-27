package com.ultimatelld.problems.pubsub.driver;

import com.ultimatelld.problems.pubsub.broker.Broker;
import com.ultimatelld.problems.pubsub.broker.ConsumerGroup;
import com.ultimatelld.problems.pubsub.core.Message;
import com.ultimatelld.problems.pubsub.core.Subscriber;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Composition root + concurrent producer/consumer simulation for the pub-sub broker.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        Broker broker = new Broker();
        broker.createTopic("orders", 3);

        // Group "analytics": 1 consumer -> owns all 3 partitions, sees every message (fan-out).
        CountingSubscriber analytics = new CountingSubscriber("analytics");
        broker.subscribe("orders", "analytics", analytics);

        // Group "billing": 2 consumers -> partitions load-balanced (p0,p2 -> c0 ; p1 -> c1).
        CountingSubscriber billing0 = new CountingSubscriber("billing-0");
        CountingSubscriber billing1 = new CountingSubscriber("billing-1");
        broker.subscribe("orders", "billing", billing0);
        broker.subscribe("orders", "billing", billing1);

        // --- concurrent producers ---
        int total = 300;
        int producers = 6;
        ExecutorService pool = Executors.newFixedThreadPool(producers);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger seq = new AtomicInteger();
        for (int t = 0; t < producers; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    int i;
                    while ((i = seq.getAndIncrement()) < total) {
                        String key = "user-" + (i % 5);   // same key -> same partition -> ordered
                        broker.publish("orders", key, "msg-" + i);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        //noinspection ResultOfMethodCallIgnored
        pool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);

        // --- begin delivery and wait until both groups have consumed everything ---
        broker.start();
        long deadline = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < deadline) {
            int billingTotal = billing0.count.get() + billing1.count.get();
            if (analytics.count.get() >= total && billingTotal >= total) break;
            Thread.sleep(10);
        }
        broker.shutdown();

        // --- results ---
        System.out.println("Published " + total + " messages across 3 partitions by " + producers + " producers");
        System.out.println("analytics group total received = " + analytics.count.get()
                + " (expected " + total + ", fan-out)");
        System.out.println("billing group total received = " + (billing0.count.get() + billing1.count.get())
                + " (expected " + total + ") -> billing-0=" + billing0.count.get()
                + " (partitions 0,2), billing-1=" + billing1.count.get() + " (partition 1)");
        System.out.println("ordering violations: analytics=" + analytics.orderingViolations.get()
                + ", billing-0=" + billing0.orderingViolations.get()
                + ", billing-1=" + billing1.orderingViolations.get() + " (expected 0 everywhere)");

        ConsumerGroup analyticsGroup = broker.group("orders", "analytics");
        StringBuilder offsets = new StringBuilder();
        for (int p = 0; p < 3; p++) {
            offsets.append("p").append(p).append("=").append(analyticsGroup.committedOffset(p)).append(" ");
        }
        System.out.println("analytics committed offsets per partition: " + offsets.toString().trim()
                + " (sum = " + total + ")");
    }

    /** Counts deliveries and verifies per-partition offsets arrive strictly increasing. */
    private static final class CountingSubscriber implements Subscriber {
        final String name;
        final AtomicInteger count = new AtomicInteger();
        final AtomicInteger orderingViolations = new AtomicInteger();
        final ConcurrentHashMap<Integer, Long> lastOffset = new ConcurrentHashMap<>();

        CountingSubscriber(String name) {
            this.name = name;
        }

        @Override
        public void onMessage(Message m) {
            count.incrementAndGet();
            lastOffset.merge(m.partition(), m.offset(), (prev, cur) -> {
                if (cur <= prev) orderingViolations.incrementAndGet();
                return cur;
            });
        }
    }
}
