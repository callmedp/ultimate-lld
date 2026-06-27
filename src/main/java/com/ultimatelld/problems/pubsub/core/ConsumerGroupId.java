package com.ultimatelld.problems.pubsub.core;

import java.util.Objects;

/** Identifies a consumer group. Different groups each receive the full stream (fan-out). */
public record ConsumerGroupId(String value) {
    public ConsumerGroupId {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) throw new IllegalArgumentException("group id must not be blank");
    }
}
