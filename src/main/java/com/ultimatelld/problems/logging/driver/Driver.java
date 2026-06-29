package com.ultimatelld.problems.logging.driver;

import com.ultimatelld.problems.logging.appender.AsyncAppender;
import com.ultimatelld.problems.logging.appender.CountingAppender;
import com.ultimatelld.problems.logging.core.LogLevel;
import com.ultimatelld.problems.logging.core.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Composition root + concurrent logging demo. Verifies level filtering and that async delivery loses
 * nothing across a clean flush on close.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        CountingAppender sink = new CountingAppender();
        AsyncAppender async = new AsyncAppender(sink);            // async wraps the terminal sink
        Logger logger = new Logger(LogLevel.INFO).addAppender(async);

        int threads = 16;
        int perThread = 10_000;
        // Each iteration logs one DEBUG (filtered) + one INFO + occasionally WARN/ERROR.
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        logger.debug("verbose trace " + i);   // below INFO -> dropped
                        logger.info("request handled " + i);
                        if (i % 100 == 0) logger.warn("slow path " + i);
                        if (i % 1000 == 0) logger.error("error sample " + i);
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

        async.close();   // flush: drains the queue, then joins the worker

        long expectedInfo = (long) threads * perThread;
        long expectedWarn = (long) threads * (perThread / 100);          // i % 100 == 0
        long expectedError = (long) threads * ((perThread + 999) / 1000); // i % 1000 == 0
        long expectedDelivered = expectedInfo + expectedWarn + expectedError;

        System.out.println("Threshold=INFO, so all DEBUG logs were filtered out before delivery.");
        System.out.println("Delivered: total=" + sink.total()
                + " (expected " + expectedDelivered + ")"
                + " -> INFO=" + sink.count(LogLevel.INFO) + ", WARN=" + sink.count(LogLevel.WARN)
                + ", ERROR=" + sink.count(LogLevel.ERROR) + ", DEBUG=" + sink.count(LogLevel.DEBUG));
        System.out.println("No messages lost across async flush? " + (sink.total() == expectedDelivered)
                + "; queue drained? " + (async.queueDepth() == 0));
    }
}
