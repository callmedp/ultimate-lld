package com.ultimatelld.theory.module02patterns.behavioral;

import com.ultimatelld.common.Money;

/** STRATEGY — applies a percentage discount to the line total. */
public final class DiscountPricing implements PricingStrategy {

    private final int percentOff;

    public DiscountPricing(int percentOff) {
        if (percentOff < 0 || percentOff > 100) {
            throw new IllegalArgumentException("percentOff must be in [0,100]");
        }
        this.percentOff = percentOff;
    }

    @Override
    public Money priceFor(Money basePrice, int quantity) {
        long gross = basePrice.multiply(quantity).minor();
        long net = gross * (100 - percentOff) / 100; // integer minor-unit math, no doubles
        return Money.of(net);
    }

    @Override
    public String name() {
        return "DISCOUNT-" + percentOff + "%";
    }
}
