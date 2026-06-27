package com.ultimatelld.theory.module02patterns.behavioral;

/** STATE — enough money is in the machine; selecting a product arms dispensing. */
public final class HasMoneyState implements VendingState {

    @Override
    public String name() {
        return "HAS_MONEY";
    }

    @Override
    public VendingState insertCoin(VendingMachine machine) {
        machine.addBalance(machine.price()); // extra coins accumulate as balance
        return this;
    }

    @Override
    public VendingState selectProduct(VendingMachine machine) {
        if (machine.balance() >= machine.price()) {
            return new DispensingState();
        }
        return this;
    }

    @Override
    public VendingState dispense(VendingMachine machine) {
        // ignored: must select a product first
        return this;
    }
}
