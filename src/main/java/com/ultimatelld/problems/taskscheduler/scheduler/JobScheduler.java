package com.ultimatelld.problems.taskscheduler.scheduler;

import com.ultimatelld.problems.taskscheduler.core.Job;
import com.ultimatelld.problems.taskscheduler.core.JobId;
import com.ultimatelld.problems.taskscheduler.core.JobWork;
import com.ultimatelld.problems.taskscheduler.core.Priority;
import com.ultimatelld.problems.taskscheduler.core.RetryPolicy;

import java.util.List;
import java.util.Comparator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Delayed;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Multi-threaded job scheduler.
 *
 * <p>Architecture (two queues + a dispatcher):
 * <ul>
 *   <li>{@link DelayQueue} holds time-scheduled jobs; a job only becomes head-of-queue once its
 *       delay elapses. A single <b>dispatcher</b> thread blocks on it and moves due jobs across.</li>
 *   <li>{@link PriorityBlockingQueue} is the <b>ready queue</b>; N <b>worker</b> threads pull from it.
 *       Its comparator orders by priority weight, then FIFO sequence — so a HIGH job jumps ahead of
 *       LOW jobs already waiting, but two HIGH jobs run in submission order.</li>
 * </ul>
 *
 * <p>Failure isolation: a throwing job never kills its worker; it is retried up to its
 * {@link RetryPolicy}, then routed to the dead-letter list. Shutdown is graceful: it stops
 * accepting, drains all outstanding work, then interrupts and joins every thread.
 */
public final class JobScheduler {

    private final DelayQueue<DelayedJob> delayQueue = new DelayQueue<>();
    private final PriorityBlockingQueue<Job> readyQueue;
    private final List<Thread> workers = new CopyOnWriteArrayList<>();
    private final Thread dispatcher;

    private final AtomicBoolean accepting = new AtomicBoolean(true);
    private final AtomicBoolean running = new AtomicBoolean(true);

    /** Outstanding = submitted but not yet terminal (SUCCEEDED/DEAD). Drives graceful drain. */
    private final AtomicInteger outstanding = new AtomicInteger(0);
    private final AtomicLong seqGen = new AtomicLong();

    private final AtomicInteger completed = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final AtomicInteger retried = new AtomicInteger();

    private final List<String> executionOrder = new CopyOnWriteArrayList<>();
    private final List<Job> deadLetter = new CopyOnWriteArrayList<>();

    public JobScheduler(int workerCount) {
        if (workerCount < 1) throw new IllegalArgumentException("workerCount must be >= 1");
        this.readyQueue = new PriorityBlockingQueue<>(64,
                Comparator.comparingInt((Job j) -> j.priority().weight()).thenComparingLong(Job::seq));

        this.dispatcher = new Thread(this::dispatchLoop, "job-dispatcher");
        this.dispatcher.start();
        for (int i = 0; i < workerCount; i++) {
            Thread w = new Thread(this::workerLoop, "job-worker-" + i);
            workers.add(w);
            w.start();
        }
    }

    public JobId submit(String name, Priority priority, RetryPolicy retryPolicy, JobWork work) {
        Job job = newJob(name, priority, retryPolicy, work);
        outstanding.incrementAndGet();
        readyQueue.offer(job);
        return job.id();
    }

    public JobId schedule(String name, Priority priority, RetryPolicy retryPolicy,
                          long delay, TimeUnit unit, JobWork work) {
        Job job = newJob(name, priority, retryPolicy, work);
        outstanding.incrementAndGet();
        long readyAt = System.nanoTime() + unit.toNanos(delay);
        delayQueue.offer(new DelayedJob(job, readyAt));
        return job.id();
    }

    private Job newJob(String name, Priority priority, RetryPolicy retryPolicy, JobWork work) {
        if (!accepting.get()) {
            throw new IllegalStateException("scheduler is shutting down; not accepting new jobs");
        }
        return new Job(JobId.next(), name, priority, work, retryPolicy, seqGen.incrementAndGet());
    }

    private void dispatchLoop() {
        while (running.get()) {
            try {
                DelayedJob due = delayQueue.poll(100, TimeUnit.MILLISECONDS);
                if (due != null) {
                    readyQueue.offer(due.job);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    private void workerLoop() {
        while (running.get()) {
            Job job;
            try {
                job = readyQueue.poll(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
            if (job == null) continue;
            runWithRetry(job);
        }
    }

    private void runWithRetry(Job job) {
        job.markRunning();
        int attempt = job.incrementAttempt();
        try {
            job.work().execute();
            job.markSucceeded();
            executionOrder.add(job.name());
            completed.incrementAndGet();
            outstanding.decrementAndGet();
        } catch (Exception ex) {
            if (attempt < job.retryPolicy().maxAttempts()) {
                retried.incrementAndGet();
                job.markPendingForRetry();
                readyQueue.offer(job);            // re-queue; failure isolated, worker survives
            } else {
                job.markDead();
                deadLetter.add(job);
                failed.incrementAndGet();
                outstanding.decrementAndGet();
            }
        }
    }

    /** Stop accepting, drain all outstanding work, then stop and join all threads. */
    public void shutdownGracefully() throws InterruptedException {
        accepting.set(false);
        while (outstanding.get() > 0) {
            Thread.sleep(5);
        }
        running.set(false);
        dispatcher.interrupt();
        workers.forEach(Thread::interrupt);
        dispatcher.join(1000);
        for (Thread w : workers) {
            w.join(1000);
        }
    }

    public int completedCount() {
        return completed.get();
    }

    public int failedCount() {
        return failed.get();
    }

    public int retriedCount() {
        return retried.get();
    }

    public List<String> executionOrder() {
        return List.copyOf(executionOrder);
    }

    public List<Job> deadLetter() {
        return List.copyOf(deadLetter);
    }

    /** Wraps a job with its ready-time so {@link DelayQueue} releases it only when due. */
    private static final class DelayedJob implements Delayed {
        private final Job job;
        private final long readyAtNanos;

        DelayedJob(Job job, long readyAtNanos) {
            this.job = job;
            this.readyAtNanos = readyAtNanos;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(readyAtNanos - System.nanoTime(), TimeUnit.NANOSECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            return Long.compare(this.readyAtNanos, ((DelayedJob) other).readyAtNanos);
        }
    }
}
