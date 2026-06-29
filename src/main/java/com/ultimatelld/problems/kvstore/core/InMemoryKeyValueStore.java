package com.ultimatelld.problems.kvstore.core;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory KV store with TTL, modelled on Redis's hybrid expiry:
 * <ul>
 *   <li><b>Lazy</b>: {@link #get} checks expiry on read and removes the dead entry on the spot.</li>
 *   <li><b>Active</b>: {@link #purgeExpired} (driven by a background {@link TtlReaper}) sweeps keys
 *       that are never read again so they don't leak memory.</li>
 * </ul>
 * Backed by a {@link ConcurrentHashMap}; the conditional {@code remove(key, entry)} ensures we never
 * delete an entry that was concurrently overwritten with a fresh value.
 */
public final class InMemoryKeyValueStore<K, V> implements KeyValueStore<K, V> {

    /** value + absolute expiry (0 = never expires). */
    private record Entry<V>(V value, long expiresAtMillis) {
        boolean isExpired(long now) {
            return expiresAtMillis > 0 && now >= expiresAtMillis;
        }
    }

    private final ConcurrentHashMap<K, Entry<V>> map = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryKeyValueStore(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public void put(K key, V value) {
        map.put(Objects.requireNonNull(key), new Entry<>(Objects.requireNonNull(value), 0));
    }

    @Override
    public void put(K key, V value, long ttlMillis) {
        if (ttlMillis <= 0) throw new IllegalArgumentException("ttlMillis must be > 0");
        long expiry = clock.nowMillis() + ttlMillis;
        map.put(Objects.requireNonNull(key), new Entry<>(Objects.requireNonNull(value), expiry));
    }

    @Override
    public Optional<V> get(K key) {
        Entry<V> entry = map.get(key);
        if (entry == null) return Optional.empty();
        if (entry.isExpired(clock.nowMillis())) {
            map.remove(key, entry);          // lazy eviction; conditional so we don't drop a fresh write
            return Optional.empty();
        }
        return Optional.of(entry.value());
    }

    @Override
    public boolean delete(K key) {
        return map.remove(key) != null;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public long purgeExpired() {
        long now = clock.nowMillis();
        long purged = 0;
        for (Map.Entry<K, Entry<V>> e : map.entrySet()) {
            if (e.getValue().isExpired(now) && map.remove(e.getKey(), e.getValue())) {
                purged++;
            }
        }
        return purged;
    }
}
