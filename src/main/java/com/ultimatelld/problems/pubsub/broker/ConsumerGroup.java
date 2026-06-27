package com.ultimatelld.problems.pubsub.broker;

import com.ultimatelld.problems.pubsub.core.ConsumerGroupId;
import com.ultimatelld.problems.pubsub.core.Subscriber;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A consumer group: a set of consumers that together consume every partition of a topic exactly
 * once (load balancing within the group), while tracking a committed offset per partition.
 * Partition {@code p} is statically assigned to consumer {@code p % consumerCount}.
 */
public final class ConsumerGroup {

    private final ConsumerGroupId id;
    private final CopyOnWriteArrayList<Subscriber> consumers = new CopyOnWriteArrayList<>();
    private final ConcurrentHashMap<Integer, AtomicLong> committedOffsets = new ConcurrentHashMap<>();

    public ConsumerGroup(ConsumerGroupId id) {
        this.id = id;
    }

    public ConsumerGroupId id() {
        return id;
    }

    public void addConsumer(Subscriber subscriber) {
        consumers.add(subscriber);
    }

    public int consumerCount() {
        return consumers.size();
    }

    /** Round-robin partition assignment: each partition is owned by exactly one consumer. */
    public Subscriber consumerForPartition(int partition) {
        return consumers.get(partition % consumers.size());
    }

    public long committedOffset(int partition) {
        return committedOffsets.computeIfAbsent(partition, k -> new AtomicLong(0)).get();
    }

    public void commit(int partition, long nextOffset) {
        committedOffsets.computeIfAbsent(partition, k -> new AtomicLong(0)).set(nextOffset);
    }

    public List<Integer> committedPartitions() {
        return List.copyOf(committedOffsets.keySet());
    }
}
