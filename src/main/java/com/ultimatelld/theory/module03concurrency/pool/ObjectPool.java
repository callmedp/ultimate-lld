package com.ultimatelld.theory.module03concurrency.pool;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A bounded, thread-safe object pool. Backed by an {@link ArrayBlockingQueue}: {@code acquire}
 * removes an object (blocking up to a timeout when empty) and {@code release} returns it. Because a
 * queue {@code poll} hands each element to exactly one caller, the pool can NEVER lend the same
 * object to two threads simultaneously — that property is what makes it safe under contention.
 *
 * @param <T> the pooled resource type
 */
public final class ObjectPool<T> {

    private final BlockingQueue<T> available;
    private final int capacity;

    public ObjectPool(List<T> resources) {
        Objects.requireNonNull(resources);
        if (resources.isEmpty()) throw new IllegalArgumentException("pool needs at least one resource");
        this.capacity = resources.size();
        this.available = new ArrayBlockingQueue<>(capacity);
        this.available.addAll(resources);
    }

    /** @return a resource, or {@code null} if none became available within the timeout. */
    public T acquire(long timeout, TimeUnit unit) throws InterruptedException {
        return available.poll(timeout, unit);
    }

    /** Returns a resource to the pool. */
    public void release(T resource) {
        Objects.requireNonNull(resource);
        if (!available.offer(resource)) {
            throw new IllegalStateException("releasing more resources than the pool's capacity");
        }
    }

    public int availableCount() {
        return available.size();
    }

    public int capacity() {
        return capacity;
    }
}
