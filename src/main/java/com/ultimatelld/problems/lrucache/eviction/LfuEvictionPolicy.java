package com.ultimatelld.problems.lrucache.eviction;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * Least-Frequently-Used ordering with LRU tie-breaking. Tracks each key's access count and groups
 * keys by frequency; the victim is the least-recently-used key within the lowest-frequency bucket.
 * Demonstrates that swapping eviction behavior is purely additive (OCP) — the cache is untouched.
 */
public final class LfuEvictionPolicy<K> implements EvictionPolicy<K> {

    private final Map<K, Integer> freq = new HashMap<>();
    /** frequency -> keys at that frequency, in LRU order (insertion order). */
    private final TreeMap<Integer, LinkedHashSet<K>> buckets = new TreeMap<>();

    @Override
    public void onInsert(K key) {
        freq.put(key, 1);
        buckets.computeIfAbsent(1, f -> new LinkedHashSet<>()).add(key);
    }

    @Override
    public void onAccess(K key) {
        Integer f = freq.get(key);
        if (f == null) {
            onInsert(key);
            return;
        }
        moveBucket(key, f, f + 1);
        freq.put(key, f + 1);
    }

    @Override
    public void onRemove(K key) {
        Integer f = freq.remove(key);
        if (f != null) {
            removeFromBucket(key, f);
        }
    }

    @Override
    public K evictCandidate() {
        Map.Entry<Integer, LinkedHashSet<K>> lowest = buckets.firstEntry();
        return lowest.getValue().iterator().next();
    }

    @Override
    public int size() {
        return freq.size();
    }

    @Override
    public String name() {
        return "LFU";
    }

    private void moveBucket(K key, int from, int to) {
        removeFromBucket(key, from);
        buckets.computeIfAbsent(to, f -> new LinkedHashSet<>()).add(key);
    }

    private void removeFromBucket(K key, int f) {
        LinkedHashSet<K> set = buckets.get(f);
        if (set != null) {
            set.remove(key);
            if (set.isEmpty()) buckets.remove(f);
        }
    }
}
