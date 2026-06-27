package com.ultimatelld.theory.module02patterns.behavioral;

import com.ultimatelld.common.Money;

/**
 * STRATEGY pattern — a family of interchangeable pricing algorithms behind one interface.
 * <p>
 * The context ({@code PriceCalculator}) holds a {@code PricingStrategy} and delegates; swapping
 * regular/discount/surge pricing is a runtime decision, and adding a new scheme is a new class
 * with no edits to the context (OCP). Contrast with an {@code if/else} over a "pricingMode" enum,
 * which would have to be edited for every new scheme.
 */
public interface PricingStrategy {

    /** @return the final price for {@code quantity} units at {@code basePrice} each. */
    Money priceFor(Money basePrice, int quantity);

    String name();
}
