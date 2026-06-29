package com.ultimatelld.problems.logging.appender;

import com.ultimatelld.problems.logging.core.LogLevel;
import com.ultimatelld.problems.logging.core.LogMessage;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A terminal sink that counts delivered messages per level (thread-safe). Stands in for a real
 * console/file appender while giving the demo verifiable numbers.
 */
public final class CountingAppender implements Appender {

    private final Map<LogLevel, AtomicLong> perLevel = new EnumMap<>(LogLevel.class);
    private final AtomicLong total = new AtomicLong();

    public CountingAppender() {
        for (LogLevel l : LogLevel.values()) perLevel.put(l, new AtomicLong());
    }

    @Override
    public void append(LogMessage message) {
        perLevel.get(message.level()).incrementAndGet();
        total.incrementAndGet();
    }

    public long total() {
        return total.get();
    }

    public long count(LogLevel level) {
        return perLevel.get(level).get();
    }
}
