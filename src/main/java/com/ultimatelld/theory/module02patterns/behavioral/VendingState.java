package com.ultimatelld.theory.module02patterns.behavioral;

/**
 * STATE pattern — each state is its own CLASS implementing this interface; the machine delegates
 * every event to its current state, and the state decides the next state.
 * <p>
 * Contrast with Module 1's {@code OrderStatus} enum + {@code canTransitionTo}: that approach keeps
 * the transition TABLE in one enum, which is great for simple, behavior-free transitions. The
 * State pattern is preferred when each state carries DIFFERENT BEHAVIOR (not just "is this jump
 * legal?") — here, inserting a coin means something different in each state. State-as-class avoids
 * a giant {@code switch(state)} inside every event handler.
 */
public interface VendingState {

    String name();

    /** Handle a coin insertion; returns the next state. */
    VendingState insertCoin(VendingMachine machine);

    /** Handle a product-selection request; returns the next state. */
    VendingState selectProduct(VendingMachine machine);

    /** Handle dispensing; returns the next state. */
    VendingState dispense(VendingMachine machine);
}
