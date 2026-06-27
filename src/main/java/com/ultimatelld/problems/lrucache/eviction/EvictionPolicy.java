package com.ultimatelld.problems.lrucache.eviction;

/**
 * OCP eviction policy. The cache owns the lock and calls these hooks; a policy is pure ordering
 * logic and need not be thread-safe itself. Adding LFU, MRU, etc. is a new class, zero cache edits.
 *
 * @param <K> key type
 */
public interface EvictionPolicy<K> {

    /** Record that a key was just inserted. */
    void onInsert(K key);

    /** Record that a key was just read or overwritten. */
    void onAccess(K key);

    /** Record that a key was removed/evicted from the cache. */
    void onRemove(K key);

    /** @return the key the policy chooses to evict next (the cache guarantees it is non-empty). */
    K evictCandidate();

    /** Number of keys the policy is currently tracking (used for consistency checks). */
    int size();

    String name();
}
