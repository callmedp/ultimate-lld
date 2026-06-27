package com.ultimatelld.theory.module01solid.repository;

import com.ultimatelld.theory.module01solid.entity.Order;
import com.ultimatelld.theory.module01solid.entity.OrderId;

import java.util.Optional;

/**
 * Persistence abstraction OWNED BY THE DOMAIN LAYER (Dependency Inversion).
 * <p>
 * The service depends on this interface; the concrete storage technology
 * (in-memory, JDBC, Mongo, ...) depends on it too. The arrow of dependency
 * points inward toward the domain — infrastructure is a plug-in, not a master.
 */
public interface OrderRepository {

    Optional<Order> findById(OrderId id);

    void save(Order order);
}
