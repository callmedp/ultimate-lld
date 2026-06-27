package com.ultimatelld.theory.module01solid.strategy;

import com.ultimatelld.common.Money;
import com.ultimatelld.theory.module01solid.entity.Order;

/**
 * Express shipping: a flat premium plus a surcharge that scales with order value.
 * Demonstrates that a strategy can carry its own non-trivial logic.
 */
public final class ExpressShipping implements ShippingStrategy {

    private static final Money BASE = Money.of(15_00); // 15.00

    @Override
    public Money cost(Order order) {
        // 2% insurance surcharge on the order value, on top of the flat premium.
        long surchargeMinor = Math.round(order.total().minor() * 0.02);
        return BASE.add(Money.of(surchargeMinor));
    }

    @Override
    public String name() {
        return "EXPRESS";
    }
}
