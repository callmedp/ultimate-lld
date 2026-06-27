package com.ultimatelld.problems.pubsub.broker;

import com.ultimatelld.problems.pubsub.core.TopicName;

/**
 * A topic split into a fixed number of partitions. The partition for a record is chosen by
 * {@code floorMod(key.hashCode(), partitionCount)} so the same key always maps to the same
 * partition — the basis of per-key ordering.
 */
public final class Topic {

    private final TopicName name;
    private final Partition[] partitions;

    public Topic(TopicName name, int partitionCount) {
        if (partitionCount < 1) throw new IllegalArgumentException("partitionCount must be >= 1");
        this.name = name;
        this.partitions = new Partition[partitionCount];
        for (int i = 0; i < partitionCount; i++) {
            partitions[i] = new Partition(name, i);
        }
    }

    public TopicName name() {
        return name;
    }

    public int partitionCount() {
        return partitions.length;
    }

    public Partition partition(int index) {
        return partitions[index];
    }

    public int partitionForKey(String key) {
        return Math.floorMod(key.hashCode(), partitions.length);
    }

    /** Publishes a record to the key's partition; returns the assigned offset. */
    public long publish(String key, String payload) {
        return partitions[partitionForKey(key)].append(key, payload);
    }
}
