package com.ultimatelld.problems.logging.core;

/** Severity levels in ascending order. A logger emits a record only if its level >= the threshold. */
public enum LogLevel {
    DEBUG,
    INFO,
    WARN,
    ERROR;

    public boolean isAtLeast(LogLevel threshold) {
        return this.ordinal() >= threshold.ordinal();
    }
}
