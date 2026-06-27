package com.ultimatelld.problems.lrucache.core;

import com.ultimatelld.problems.lrucache.eviction.EvictionPolicy;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A fixed-capacity, thread-safe cache. The cache owns a single {@link ReentrantLock} that guards
 * BOTH the backing map and the {@link EvictionPolicy}, so the read/overwrite/evict/insert sequence
 * is atomic — capacity is never exceeded and the map and policy can never drift out of sync, even
 * under heavy concurrent access. Eviction behavior is injected (LRU, LFU, ...) — Strategy + OCP.
 */
public final class BoundedCache<K, V> implements Cache<K, V> {

    private final int capacity;
    private final Map<K, V> store = new HashMap<>();
    private final EvictionPolicy<K> policy;
    private final ReentrantLock lock = new ReentrantLock();

    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();
    private final AtomicLong evictions = new AtomicLong();

    public BoundedCache(int capacity, EvictionPolicy<K> policy) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.capacity = capacity;
        this.policy = Objects.requireNonNull(policy);
    }

    @Override
    public Optional<V> get(K key) {
        Objects.requireNonNull(key);
        lock.lock();
        try {
            V value = store.get(key);
            if (value == null) {
                misses.incrementAndGet();
                return Optional.empty();
            }
            policy.onAccess(key);
            hits.incrementAndGet();
            return Optional.of(value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(K key, V value) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        lock.lock();
        try {
            if (store.containsKey(key)) {
                store.put(key, value);
                policy.onAccess(key);
                return;
            }
            if (store.size() >= capacity) {
                K victim = policy.evictCandidate();
                store.remove(victim);
                policy.onRemove(victim);
                evictions.incrementAndGet();
            }
            store.put(key, value);
            policy.onInsert(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return store.size();
        } finally {
            lock.unlock();
        }
    }

    /** True iff the backing map and the eviction policy agree on the tracked key set size. */
    public boolean isConsistent() {
        lock.lock();
        try {
            return store.size() == policy.size() && store.size() <= capacity;
        } finally {
            lock.unlock();
        }
    }

    public long hits() {
        return hits.get();
    }

    public long misses() {
        return misses.get();
    }

    public long evictions() {
        return evictions.get();
    }
}
