package com.ultimatelld.problems.kvstore.core;

import java.util.Optional;

/** A concurrent key-value store with optional per-key TTL (time-to-live). */
public interface KeyValueStore<K, V> {

    void put(K key, V value);

    /** Store with a time-to-live in milliseconds; the entry expires after that elapses. */
    void put(K key, V value, long ttlMillis);

    Optional<V> get(K key);

    boolean delete(K key);

    /** Logical entry count (may transiently include not-yet-purged expired entries). */
    int size();

    /** Proactively remove all expired entries; returns how many were purged. */
    long purgeExpired();
}
