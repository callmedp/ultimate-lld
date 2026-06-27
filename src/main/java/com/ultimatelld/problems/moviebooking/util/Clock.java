package com.ultimatelld.problems.moviebooking.util;

/** Injectable clock so hold-expiry is deterministic in tests/demos (never read wall time directly). */
public interface Clock {
    long nowMillis();
}
