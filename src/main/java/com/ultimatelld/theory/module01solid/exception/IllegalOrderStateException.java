package com.ultimatelld.theory.module01solid.exception;

import com.ultimatelld.theory.module01solid.entity.OrderStatus;

/**
 * Thrown when a caller attempts an illegal lifecycle transition on an Order.
 * A domain-specific exception communicates intent far better than a raw
 * {@link IllegalStateException}.
 */
public class IllegalOrderStateException extends RuntimeException {

    public IllegalOrderStateException(OrderStatus from, OrderStatus to) {
        super("Illegal order transition: " + from + " -> " + to);
    }
}
