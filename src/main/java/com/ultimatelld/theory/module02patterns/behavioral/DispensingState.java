package com.ultimatelld.theory.module02patterns.behavioral;

/** STATE — product selected and paid; dispensing completes the transaction and returns to IDLE. */
public final class DispensingState implements VendingState {

    @Override
    public String name() {
        return "DISPENSING";
    }

    @Override
    public VendingState insertCoin(VendingMachine machine) {
        // ignored: busy dispensing
        return this;
    }

    @Override
    public VendingState selectProduct(VendingMachine machine) {
        // ignored: busy dispensing
        return this;
    }

    @Override
    public VendingState dispense(VendingMachine machine) {
        machine.recordDispense();
        machine.resetBalance();
        return new IdleState();
    }
}
