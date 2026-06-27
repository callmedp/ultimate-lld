package com.ultimatelld.theory.module01solid.entity;

import com.ultimatelld.common.Money;

import java.util.Objects;

/**
 * A single line item within an order. Immutable: a line never changes once created;
 * mutating an order means adding/removing lines, not editing them in place.
 */
public record OrderLine(String sku, Money unitPrice, int quantity) {

    public OrderLine {
        Objects.requireNonNull(sku, "sku");
        Objects.requireNonNull(unitPrice, "unitPrice");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
    }

    /** Behavior, not just data — the line knows how to compute its own subtotal. */
    public Money subtotal() {
        return unitPrice.multiply(quantity);
    }
}
