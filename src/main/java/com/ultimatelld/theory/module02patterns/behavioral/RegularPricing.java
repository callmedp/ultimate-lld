package com.ultimatelld.theory.module02patterns.behavioral;

import com.ultimatelld.common.Money;

/** STRATEGY — base price, no adjustment. */
public final class RegularPricing implements PricingStrategy {

    @Override
    public Money priceFor(Money basePrice, int quantity) {
        return basePrice.multiply(quantity);
    }

    @Override
    public String name() {
        return "REGULAR";
    }
}
