package com.ultimatelld.problems.logging.core;

import java.util.Objects;

/** An immutable log record carrying its level, text, originating thread, and timestamp. */
public record LogMessage(LogLevel level, String text, String threadName, long timestampMillis) {
    public LogMessage {
        Objects.requireNonNull(level);
        Objects.requireNonNull(text);
        Objects.requireNonNull(threadName);
    }

    @Override
    public String toString() {
        return "[" + level + "] (" + threadName + ") " + text;
    }
}
