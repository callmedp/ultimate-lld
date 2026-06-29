package com.ultimatelld.problems.logging.appender;

import com.ultimatelld.problems.logging.core.LogMessage;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Decorates another appender with asynchronous delivery: {@link #append} just enqueues (the calling
 * thread never blocks on I/O), and a single background worker drains the queue into the delegate.
 * {@link #close()} performs a clean flush — it stops accepting, lets the worker drain everything
 * still queued, then joins — so no message is lost on shutdown.
 */
public final class AsyncAppender implements Appender, AutoCloseable {

    private final Appender delegate;
    private final BlockingQueue<LogMessage> queue = new LinkedBlockingQueue<>();
    private final Thread worker;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public AsyncAppender(Appender delegate) {
        this.delegate = Objects.requireNonNull(delegate);
        this.worker = new Thread(this::drainLoop, "async-appender");
        this.worker.start();
    }

    @Override
    public void append(LogMessage message) {
        queue.offer(message);   // unbounded queue -> non-blocking, never rejects
    }

    private void drainLoop() {
        // Keep draining while running, and after stop until the queue is fully flushed.
        while (running.get() || !queue.isEmpty()) {
            try {
                LogMessage msg = queue.poll(50, TimeUnit.MILLISECONDS);
                if (msg != null) {
                    delegate.append(msg);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Override
    public void close() {
        running.set(false);
        try {
            worker.join(5000);   // wait for the flush to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public int queueDepth() {
        return queue.size();
    }
}
