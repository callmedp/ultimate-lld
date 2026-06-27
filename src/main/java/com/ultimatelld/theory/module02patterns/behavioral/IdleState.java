package com.ultimatelld.theory.module02patterns.behavioral;

/** STATE — no money inserted yet. Only a coin insertion makes progress. */
public final class IdleState implements VendingState {

    @Override
    public String name() {
        return "IDLE";
    }

    @Override
    public VendingState insertCoin(VendingMachine machine) {
        machine.addBalance(machine.price()); // each coin == one unit of price for the demo
        return new HasMoneyState();
    }

    @Override
    public VendingState selectProduct(VendingMachine machine) {
        // ignored: cannot select without paying
        return this;
    }

    @Override
    public VendingState dispense(VendingMachine machine) {
        // ignored: nothing to dispense
        return this;
    }
}
