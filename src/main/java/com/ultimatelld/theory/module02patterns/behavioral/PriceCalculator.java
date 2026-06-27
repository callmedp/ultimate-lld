package com.ultimatelld.theory.module02patterns.behavioral;

import com.ultimatelld.common.Money;

import java.util.Objects;

/**
 * STRATEGY — the CONTEXT. Holds a pricing strategy (injected) and delegates the algorithm to it.
 * The strategy can be swapped at runtime via {@link #withStrategy} (returns a new context to keep
 * this object effectively immutable / safe to share).
 */
public final class PriceCalculator {

    private final PricingStrategy strategy;

    public PriceCalculator(PricingStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "strategy");
    }

    public PriceCalculator withStrategy(PricingStrategy newStrategy) {
        return new PriceCalculator(newStrategy);
    }

    public Money quote(Money basePrice, int quantity) {
        Objects.requireNonNull(basePrice, "basePrice");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        return strategy.priceFor(basePrice, quantity);
    }

    public String strategyName() {
        return strategy.name();
    }
}
