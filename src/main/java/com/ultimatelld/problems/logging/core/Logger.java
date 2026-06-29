package com.ultimatelld.problems.logging.core;

import com.ultimatelld.problems.logging.appender.Appender;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The logging front-end. Thread-safe: the appender list is a {@link CopyOnWriteArrayList} and the
 * threshold is volatile, so logging from many threads (and reconfiguring at runtime) is safe.
 * Level filtering happens here; delivery is fanned out to every configured appender.
 */
public final class Logger {

    private volatile LogLevel threshold;
    private final CopyOnWriteArrayList<Appender> appenders = new CopyOnWriteArrayList<>();

    public Logger(LogLevel threshold) {
        this.threshold = Objects.requireNonNull(threshold);
    }

    public void setThreshold(LogLevel threshold) {
        this.threshold = Objects.requireNonNull(threshold);
    }

    public Logger addAppender(Appender appender) {
        appenders.add(Objects.requireNonNull(appender));
        return this;
    }

    public void log(LogLevel level, String text) {
        if (!level.isAtLeast(threshold)) {
            return;   // filtered out below the threshold
        }
        LogMessage message = new LogMessage(level, text, Thread.currentThread().getName(),
                System.currentTimeMillis());
        for (Appender appender : appenders) {
            appender.append(message);
        }
    }

    public void debug(String text) {
        log(LogLevel.DEBUG, text);
    }

    public void info(String text) {
        log(LogLevel.INFO, text);
    }

    public void warn(String text) {
        log(LogLevel.WARN, text);
    }

    public void error(String text) {
        log(LogLevel.ERROR, text);
    }

    public List<Appender> appenders() {
        return List.copyOf(appenders);
    }
}
