package com.ultimatelld.theory.module01solid.repository;

import com.ultimatelld.theory.module01solid.entity.Order;
import com.ultimatelld.theory.module01solid.entity.OrderId;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory implementation — the standard choice for LLD interview
 * simulations. {@link ConcurrentHashMap} gives us safe concurrent reads/writes
 * without a global lock around the whole store.
 */
public final class InMemoryOrderRepository implements OrderRepository {

    private final ConcurrentHashMap<OrderId, Order> store = new ConcurrentHashMap<>();

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void save(Order order) {
        store.put(order.id(), order);
    }
}
