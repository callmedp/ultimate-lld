package com.ultimatelld.problems.pubsub.broker;

import com.ultimatelld.problems.pubsub.core.ConsumerGroupId;
import com.ultimatelld.problems.pubsub.core.Message;
import com.ultimatelld.problems.pubsub.core.Subscriber;
import com.ultimatelld.problems.pubsub.core.TopicName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * In-memory pub-sub broker (Kafka-lite). Facade over topics, partitions, and consumer groups.
 *
 * <p>Delivery model: when {@link #start()} is called, the broker spawns one delivery thread per
 * (group, partition) pair. Each thread reads the partition log from the group's committed offset,
 * delivers to the consumer assigned that partition, and commits the offset only AFTER the handler
 * returns — giving <b>at-least-once</b> semantics (a crash between deliver and commit causes
 * redelivery, never loss). Because one thread owns a partition for a group, per-partition (and thus
 * per-key) ordering is preserved.
 */
public final class Broker {

    private final ConcurrentHashMap<TopicName, Topic> topics = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<TopicName, ConcurrentHashMap<ConsumerGroupId, ConsumerGroup>> groups =
            new ConcurrentHashMap<>();

    private final List<Thread> deliveryThreads = new ArrayList<>();
    private final AtomicBoolean running = new AtomicBoolean(false);

    public void createTopic(String name, int partitions) {
        TopicName topic = new TopicName(name);
        topics.put(topic, new Topic(topic, partitions));
        groups.put(topic, new ConcurrentHashMap<>());
    }

    public long publish(String topicName, String key, String payload) {
        return topic(topicName).publish(key, payload);
    }

    public void subscribe(String topicName, String groupId, Subscriber subscriber) {
        TopicName topic = new TopicName(topicName);
        ConsumerGroupId gid = new ConsumerGroupId(groupId);
        groups.get(topic)
                .computeIfAbsent(gid, ConsumerGroup::new)
                .addConsumer(subscriber);
    }

    /** Begins delivery. Assignments are fixed at this point, so subscribe before calling start(). */
    public void start() {
        if (!running.compareAndSet(false, true)) return;
        for (Map.Entry<TopicName, Topic> e : topics.entrySet()) {
            Topic topic = e.getValue();
            for (ConsumerGroup group : groups.get(e.getKey()).values()) {
                for (int p = 0; p < topic.partitionCount(); p++) {
                    final int partition = p;
                    Thread t = new Thread(() -> deliverLoop(topic, group, partition),
                            "deliver-" + topic.name().value() + "-g-" + group.id().value() + "-p" + p);
                    deliveryThreads.add(t);
                    t.start();
                }
            }
        }
    }

    private void deliverLoop(Topic topic, ConsumerGroup group, int partition) {
        Partition log = topic.partition(partition);
        while (running.get()) {
            long offset = group.committedOffset(partition);
            Message msg = log.read(offset);
            if (msg == null) {
                sleepQuietly();
                continue;
            }
            Subscriber consumer = group.consumerForPartition(partition);
            try {
                consumer.onMessage(msg);
                group.commit(partition, offset + 1);   // commit AFTER success => at-least-once
            } catch (Exception handlerError) {
                // Do not advance the offset: the message will be redelivered. Back off briefly.
                sleepQuietly();
            }
        }
    }

    private void sleepQuietly() {
        try {
            Thread.sleep(2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running.set(false);
        }
    }

    public void shutdown() {
        running.set(false);
        deliveryThreads.forEach(Thread::interrupt);
        for (Thread t : deliveryThreads) {
            try {
                t.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public ConsumerGroup group(String topicName, String groupId) {
        return groups.get(new TopicName(topicName)).get(new ConsumerGroupId(groupId));
    }

    public Topic topic(String topicName) {
        Topic t = topics.get(new TopicName(topicName));
        if (t == null) throw new IllegalArgumentException("no such topic: " + topicName);
        return t;
    }
}
