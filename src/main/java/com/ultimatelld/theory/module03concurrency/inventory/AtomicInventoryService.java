package com.ultimatelld.theory.module03concurrency.inventory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Correct lock-free fix: a compare-and-set retry loop on an {@link AtomicInteger}. The decrement
 * only commits if the stock is still exactly what we read, so the check-and-decrement is effectively
 * atomic. Stock can never go below zero and a unit can never be sold twice.
 */
public final class AtomicInventoryService implements InventoryService {

    private final AtomicInteger stock;

    public AtomicInventoryService(int initialStock) {
        this.stock = new AtomicInteger(initialStock);
    }

    @Override
    public boolean reserve() {
        while (true) {
            int current = stock.get();
            if (current <= 0) {
                return false;
            }
            if (stock.compareAndSet(current, current - 1)) {
                return true;
            }
            // lost the race; loop and re-read
        }
    }

    @Override
    public int remaining() {
        return stock.get();
    }

    @Override
    public String label() {
        return "FIXED (AtomicInteger CAS loop)";
    }
}
