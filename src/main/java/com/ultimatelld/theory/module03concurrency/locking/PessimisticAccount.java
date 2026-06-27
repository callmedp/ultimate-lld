package com.ultimatelld.theory.module03concurrency.locking;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Pessimistic concurrency: acquire a lock before touching shared state. Simple and predictable;
 * threads block rather than retry. Best when contention is high or the critical section is large,
 * where optimistic retries would thrash.
 */
public final class PessimisticAccount {

    private final ReentrantLock lock = new ReentrantLock();
    private long balance;

    public void deposit(long amount) {
        if (amount <= 0) throw new IllegalArgumentException("amount must be positive");
        lock.lock();
        try {
            balance += amount;
        } finally {
            lock.unlock();
        }
    }

    public long balance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }
}
