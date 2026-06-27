package com.ultimatelld.problems.pubsub.core;

import java.util.Objects;

/**
 * An immutable message. {@code offset} is its position within its partition's append-only log;
 * messages sharing a {@code key} always land in the same partition, preserving per-key ordering.
 */
public record Message(TopicName topic, int partition, long offset, String key, String payload) {
    public Message {
        Objects.requireNonNull(topic, "topic");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(payload, "payload");
        if (partition < 0) throw new IllegalArgumentException("partition must be >= 0");
        if (offset < 0) throw new IllegalArgumentException("offset must be >= 0");
    }
}
