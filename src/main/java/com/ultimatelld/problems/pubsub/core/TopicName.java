package com.ultimatelld.problems.pubsub.core;

import java.util.Objects;

/** Strongly-typed topic name. */
public record TopicName(String value) {
    public TopicName {
        Objects.requireNonNull(value, "value");
        if (value.isBlank()) throw new IllegalArgumentException("topic name must not be blank");
    }
}
