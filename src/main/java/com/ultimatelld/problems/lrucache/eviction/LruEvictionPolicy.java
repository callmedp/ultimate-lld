package com.ultimatelld.problems.lrucache.eviction;

import java.util.LinkedHashSet;

/**
 * Least-Recently-Used ordering. A {@link LinkedHashSet} preserves insertion order; re-inserting an
 * existing key moves it to the tail (most-recently-used). The head is therefore the LRU victim.
 * O(1) for all operations.
 */
public final class LruEvictionPolicy<K> implements EvictionPolicy<K> {

    private final LinkedHashSet<K> order = new LinkedHashSet<>();

    @Override
    public void onInsert(K key) {
        order.add(key);
    }

    @Override
    public void onAccess(K key) {
        order.remove(key);   // move to tail = most recently used
        order.add(key);
    }

    @Override
    public void onRemove(K key) {
        order.remove(key);
    }

    @Override
    public K evictCandidate() {
        return order.iterator().next();   // head = least recently used
    }

    @Override
    public int size() {
        return order.size();
    }

    @Override
    public String name() {
        return "LRU";
    }
}
