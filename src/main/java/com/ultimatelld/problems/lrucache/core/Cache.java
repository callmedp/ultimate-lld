package com.ultimatelld.problems.lrucache.core;

import java.util.Optional;

/** Generic cache abstraction. */
public interface Cache<K, V> {
    Optional<V> get(K key);

    void put(K key, V value);

    int size();
}
