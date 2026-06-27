package com.ultimatelld.theory.module01solid.entity;

import java.util.Set;

/**
 * The lifecycle of an order, expressed as an explicit state machine.
 * <p>
 * Encapsulating the legal transitions inside the enum (rather than scattering
 * {@code if (status == X)} checks across services) is the State-pattern-lite that
 * keeps the rule in exactly ONE place — the heart of a rich domain model.
 */
public enum OrderStatus {
    CREATED,
    PAID,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    private Set<OrderStatus> allowedNext() {
        return switch (this) {
            case CREATED   -> Set.of(PAID, CANCELLED);
            case PAID      -> Set.of(SHIPPED, CANCELLED);
            case SHIPPED   -> Set.of(DELIVERED);
            case DELIVERED -> Set.of();
            case CANCELLED -> Set.of();
        };
    }

    public boolean canTransitionTo(OrderStatus target) {
        return allowedNext().contains(target);
    }
}
