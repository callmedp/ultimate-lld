package com.ultimatelld.theory.module03concurrency.inventory;

/**
 * INTENTIONALLY BROKEN — demonstrates a classic lost-update / oversell race.
 * The read ({@code stock}), the check ({@code > 0}), and the write ({@code stock = s - 1}) are
 * three separate steps with no atomicity. Two threads can both read the same positive value, both
 * pass the check, and both decrement — selling the same unit twice and driving stock negative.
 */
public final class BrokenInventoryService implements InventoryService {

    private int stock;

    public BrokenInventoryService(int initialStock) {
        this.stock = initialStock;
    }

    @Override
    public boolean reserve() {
        int s = stock;                 // read
        if (s <= 0) {
            return false;              // check
        }
        Thread.onSpinWait();           // widen the race window to make the bug observable
        stock = s - 1;                 // write (lost update happens here)
        return true;
    }

    @Override
    public int remaining() {
        return stock;
    }

    @Override
    public String label() {
        return "BROKEN (plain int, no atomicity)";
    }
}
