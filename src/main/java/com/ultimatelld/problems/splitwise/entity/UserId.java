package com.ultimatelld.problems.splitwise.entity;

import java.util.Objects;

/** Strongly-typed user identifier. */
public record UserId(String value) {
    public UserId {
        Objects.requireNonNull(value);
        if (value.isBlank()) throw new IllegalArgumentException("user id must not be blank");
    }

    public static UserId of(String v) {
        return new UserId(v);
    }
}
