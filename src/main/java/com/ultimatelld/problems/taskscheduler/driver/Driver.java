package com.ultimatelld.problems.taskscheduler.driver;

import com.ultimatelld.problems.taskscheduler.core.Priority;
import com.ultimatelld.problems.taskscheduler.core.RetryPolicy;
import com.ultimatelld.problems.taskscheduler.scheduler.JobScheduler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Composition root + behavioral demo for the job scheduler.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        // ---- Demos 1-4 use a single worker so the ready-queue's priority order is observable ----
        JobScheduler scheduler = new JobScheduler(1);

        // (1) Priority ordering: occupy the worker with a gate, enqueue LOW/MED/HIGH, then release.
        CountDownLatch gate = new CountDownLatch(1);
        scheduler.submit("gate", Priority.HIGH, RetryPolicy.noRetry(), gate::await);
        Thread.sleep(50); // let the worker pick up the gate job
        scheduler.submit("low-task", Priority.LOW, RetryPolicy.noRetry(), () -> {});
        scheduler.submit("medium-task", Priority.MEDIUM, RetryPolicy.noRetry(), () -> {});
        scheduler.submit("high-task", Priority.HIGH, RetryPolicy.noRetry(), () -> {});
        gate.countDown();

        // (2) Delayed execution.
        AtomicLong scheduledAt = new AtomicLong(System.currentTimeMillis());
        AtomicLong ranAt = new AtomicLong();
        scheduler.schedule("delayed-task", Priority.HIGH, RetryPolicy.noRetry(), 200, TimeUnit.MILLISECONDS,
                () -> ranAt.set(System.currentTimeMillis()));

        // (3) Retry then succeed: fails twice, succeeds on the 3rd attempt.
        AtomicInteger tries = new AtomicInteger();
        scheduler.submit("flaky-task", Priority.MEDIUM, RetryPolicy.ofAttempts(3), () -> {
            if (tries.incrementAndGet() <= 2) throw new RuntimeException("transient failure #" + tries.get());
        });

        // (4) Always fails -> dead letter after exhausting attempts.
        scheduler.submit("poison-task", Priority.MEDIUM, RetryPolicy.ofAttempts(2), () -> {
            throw new RuntimeException("permanent failure");
        });

        scheduler.shutdownGracefully();

        System.out.println("Execution order (single worker): " + scheduler.executionOrder());
        System.out.println("  -> high-task should appear before medium-task before low-task");
        long elapsed = ranAt.get() - scheduledAt.get();
        System.out.println("Delayed task ran after ~" + elapsed + "ms (target 200ms)");
        System.out.println("flaky-task attempts=" + tries.get() + " (failed twice, then succeeded)");
        System.out.println("Counters -> completed=" + scheduler.completedCount()
                + ", retried=" + scheduler.retriedCount()
                + ", failed=" + scheduler.failedCount());
        System.out.println("Dead-letter jobs: " + scheduler.deadLetter());

        // ---- Demo 5: real multi-worker throughput ----
        JobScheduler pool = new JobScheduler(4);
        int n = 100;
        AtomicInteger ran = new AtomicInteger();
        for (int i = 0; i < n; i++) {
            pool.submit("bulk-" + i, Priority.LOW, RetryPolicy.noRetry(), ran::incrementAndGet);
        }
        pool.shutdownGracefully();
        System.out.println("Throughput pool (4 workers): ran " + ran.get() + "/" + n
                + " jobs, completed=" + pool.completedCount());
    }
}
