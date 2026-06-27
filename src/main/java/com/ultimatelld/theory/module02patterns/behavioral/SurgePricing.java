package com.ultimatelld.theory.module02patterns.behavioral;

import com.ultimatelld.common.Money;

/** STRATEGY — applies a surge multiplier (expressed in basis points, e.g. 15000 = 1.5x). */
public final class SurgePricing implements PricingStrategy {

    private final int multiplierBasisPoints; // 10000 = 1.0x

    public SurgePricing(int multiplierBasisPoints) {
        if (multiplierBasisPoints < 10000) {
            throw new IllegalArgumentException("surge multiplier must be >= 1.0x (10000 bps)");
        }
        this.multiplierBasisPoints = multiplierBasisPoints;
    }

    @Override
    public Money priceFor(Money basePrice, int quantity) {
        long gross = basePrice.multiply(quantity).minor();
        long surged = gross * multiplierBasisPoints / 10000;
        return Money.of(surged);
    }

    @Override
    public String name() {
        return "SURGE-" + (multiplierBasisPoints / 100) + "%";
    }
}
