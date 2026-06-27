package com.ultimatelld.problems.pubsub.broker;

import com.ultimatelld.problems.pubsub.core.Message;
import com.ultimatelld.problems.pubsub.core.TopicName;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An append-only partition log. Appends are synchronized so each message gets a unique, gap-free
 * offset; reads are lock-free over a {@link CopyOnWriteArrayList} (safe to read while others append).
 */
public final class Partition {

    private final TopicName topic;
    private final int index;
    private final CopyOnWriteArrayList<Message> log = new CopyOnWriteArrayList<>();

    public Partition(TopicName topic, int index) {
        this.topic = topic;
        this.index = index;
    }

    /** Appends a record and returns its assigned offset. */
    public synchronized long append(String key, String payload) {
        long offset = log.size();
        log.add(new Message(topic, index, offset, key, payload));
        return offset;
    }

    /** @return the message at {@code offset}, or null if nothing has been appended there yet. */
    public Message read(long offset) {
        if (offset < 0 || offset >= log.size()) return null;
        return log.get((int) offset);
    }

    public int size() {
        return log.size();
    }

    public int index() {
        return index;
    }
}
