package com.ultimatelld.problems.ratelimiter.core;

/**
 * Strategy abstraction (OCP): each algorithm — token bucket, leaky bucket, sliding window —
 * is a separate implementation. Adding one requires zero edits to existing limiters or callers.
 */
public interface RateLimiter {

    /** @return true if the request for {@code clientId} is permitted, false if throttled. */
    boolean allow(String clientId);

    String algorithm();
}
