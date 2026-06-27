package com.ultimatelld.theory.module01solid.strategy;

import com.ultimatelld.common.Money;
import com.ultimatelld.theory.module01solid.entity.Order;

/** Flat-rate standard shipping. */
public final class StandardShipping implements ShippingStrategy {

    private static final Money FLAT_RATE = Money.of(5_00); // 5.00

    @Override
    public Money cost(Order order) {
        return FLAT_RATE;
    }

    @Override
    public String name() {
        return "STANDARD";
    }
}
