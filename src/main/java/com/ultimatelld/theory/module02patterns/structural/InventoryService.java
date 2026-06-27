package com.ultimatelld.theory.module02patterns.structural;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/** FACADE subsystem part — reserves stock. One of several services the facade hides. */
public final class InventoryService {

    private final ConcurrentHashMap<String, Integer> stock = new ConcurrentHashMap<>();

    public InventoryService(Map<String, Integer> initialStock) {
        Objects.requireNonNull(initialStock, "initialStock");
        stock.putAll(initialStock);
    }

    /** Atomically reserve {@code quantity} of {@code sku}. @return true on success. */
    public boolean reserve(String sku, int quantity) {
        Objects.requireNonNull(sku, "sku");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        AtomicBoolean reserved = new AtomicBoolean(false);
        stock.computeIfPresent(sku, (k, available) -> {
            if (available >= quantity) {
                reserved.set(true);
                return available - quantity;
            }
            return available;
        });
        return reserved.get();
    }

    /** Compensating action — returns reserved quantity to stock (used on partial-failure rollback). */
    public void release(String sku, int quantity) {
        Objects.requireNonNull(sku, "sku");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        stock.merge(sku, quantity, Integer::sum);
    }

    public int available(String sku) {
        return stock.getOrDefault(sku, 0);
    }
}
