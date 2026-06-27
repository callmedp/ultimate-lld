package com.ultimatelld.theory.module03concurrency.locking;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Optimistic concurrency: no locks. Each mutation reads the current versioned {@link State},
 * computes the next state, and publishes it with a compare-and-set. If another thread changed the
 * state in between, the CAS fails and we retry. Cheap under low contention; retries pile up under
 * high contention (the {@link #retries()} counter exposes that cost).
 */
public final class OptimisticAccount {

    /** Immutable snapshot: balance plus a version that increments on every successful update. */
    private record State(long balance, long version) {
    }

    private final AtomicReference<State> state = new AtomicReference<>(new State(0, 0));
    private final AtomicLong retries = new AtomicLong();

    public void deposit(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        while (true) {
            State current = state.get();
            State next = new State(current.balance() + amount, current.version() + 1);
            if (state.compareAndSet(current, next)) {
                return;
            }
            retries.incrementAndGet();   // someone else won the race; recompute and try again
        }
    }

    public long balance() {
        return state.get().balance();
    }

    public long version() {
        return state.get().version();
    }

    public long retries() {
        return retries.get();
    }
}
