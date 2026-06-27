package com.ultimatelld.problems.ratelimiter.middleware;

import com.ultimatelld.problems.ratelimiter.core.RateLimiter;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Facade that wraps a downstream call with rate limiting. Demonstrates clean object boundaries:
 * the caller knows nothing about buckets — it just gets a {@link Response} that is either the
 * downstream result or a 429-style rejection.
 */
public final class ThrottlingMiddleware {

    private final RateLimiter limiter;

    public ThrottlingMiddleware(RateLimiter limiter) {
        this.limiter = Objects.requireNonNull(limiter);
    }

    public <R> Response<R> handle(String clientId, Supplier<R> downstream) {
        if (!limiter.allow(clientId)) {
            return Response.throttled();
        }
        return Response.ok(downstream.get());
    }

    /** Immutable result: either OK with a body, or a 429 throttled response. */
    public static final class Response<R> {
        private final int status;
        private final R body;

        private Response(int status, R body) {
            this.status = status;
            this.body = body;
        }

        static <R> Response<R> ok(R body) {
            return new Response<>(200, body);
        }

        static <R> Response<R> throttled() {
            return new Response<>(429, null);
        }

        public boolean isThrottled() {
            return status == 429;
        }

        public int status() {
            return status;
        }

        public R body() {
            return body;
        }
    }
}
