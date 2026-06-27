package com.ultimatelld.theory.module02patterns.behavioral;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * STATE pattern — the CONTEXT. Holds the current {@link VendingState} and forwards each event to
 * it; the returned value becomes the new state. The context never contains an {@code if/switch}
 * over the state — all transition logic lives in the state classes.
 * <p>
 * Thread-safe: a {@link ReentrantLock} serializes events so the state field is mutated atomically
 * (a vending machine has exactly one physical coin slot — events must not interleave).
 */
public final class VendingMachine {

    private final ReentrantLock lock = new ReentrantLock();
    private final int price;
    private int balance;
    private int dispensed;
    private VendingState state;

    public VendingMachine(int price) {
        if (price <= 0) throw new IllegalArgumentException("price must be > 0");
        this.price = price;
        this.state = new IdleState();
    }

    public void insertCoin() {
        lock.lock();
        try {
            this.state = state.insertCoin(this);
        } finally {
            lock.unlock();
        }
    }

    public void selectProduct() {
        lock.lock();
        try {
            this.state = state.selectProduct(this);
        } finally {
            lock.unlock();
        }
    }

    public void dispense() {
        lock.lock();
        try {
            this.state = state.dispense(this);
        } finally {
            lock.unlock();
        }
    }

    public String currentState() {
        lock.lock();
        try {
            return state.name();
        } finally {
            lock.unlock();
        }
    }

    // --- package-private hooks the states use to mutate context data ---

    void addBalance(int amount) {
        this.balance += amount;
    }

    int balance() {
        return balance;
    }

    int price() {
        return price;
    }

    void resetBalance() {
        this.balance = 0;
    }

    void recordDispense() {
        this.dispensed++;
    }

    public int dispensedCount() {
        return dispensed;
    }

    static VendingState requireState(VendingState s) {
        return Objects.requireNonNull(s, "state");
    }
}
