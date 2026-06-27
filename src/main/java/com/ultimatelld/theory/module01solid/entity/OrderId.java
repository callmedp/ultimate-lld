package com.ultimatelld.theory.module01solid.entity;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifier. Prevents "stringly-typed" bugs where an OrderId,
 * UserId, and ProductId are all just {@code String} and get accidentally swapped.
 */
public record OrderId(String value) {

    public OrderId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) throw new IllegalArgumentException("OrderId must not be blank");
    }

    public static OrderId newId() {
        return new OrderId(UUID.randomUUID().toString());
    }
}
