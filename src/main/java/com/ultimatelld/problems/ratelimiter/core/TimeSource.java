package com.ultimatelld.problems.ratelimiter.core;

/**
 * Abstraction over the clock so refill/leak timing is deterministic in tests and demos.
 * Injecting time is the senior move — never read the wall clock directly inside algorithms.
 */
public interface TimeSource {
    long nanoTime();
}
