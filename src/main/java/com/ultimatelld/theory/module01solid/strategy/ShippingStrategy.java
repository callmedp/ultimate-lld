package com.ultimatelld.theory.module01solid.strategy;

import com.ultimatelld.common.Money;
import com.ultimatelld.theory.module01solid.entity.Order;

/**
 * Open/Closed Principle in action. Adding a new shipping option means adding a new
 * class that implements this interface — with ZERO modification to existing code.
 */
public interface ShippingStrategy {

    Money cost(Order order);

    String name();
}
