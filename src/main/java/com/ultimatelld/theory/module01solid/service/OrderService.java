package com.ultimatelld.theory.module01solid.service;

import com.ultimatelld.common.Money;
import com.ultimatelld.theory.module01solid.entity.Order;
import com.ultimatelld.theory.module01solid.entity.OrderId;
import com.ultimatelld.theory.module01solid.entity.OrderLine;
import com.ultimatelld.theory.module01solid.repository.OrderRepository;
import com.ultimatelld.theory.module01solid.strategy.ShippingStrategy;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Service ORCHESTRATES; it does not own business rules — those live on the entity.
 * Its job: load -> invoke a domain method -> persist -> (optionally) notify.
 * <p>
 * Dependencies are abstractions, injected via the constructor (DIP + explicit DI).
 * <p>
 * Concurrency: mutating an Order is a read-modify-write, which is not atomic. We guard
 * each order with its own lock (lock striping by id) so operations on DIFFERENT orders
 * never block each other, while operations on the SAME order are serialized. This is the
 * Module-3 concurrency model in miniature.
 */
public final class OrderService {

    private final OrderRepository repository;
    private final ShippingStrategy shippingStrategy;

    /** One lock per order id — fine-grained striping instead of a single global lock. */
    private final ConcurrentHashMap<OrderId, Lock> locks = new ConcurrentHashMap<>();

    public OrderService(OrderRepository repository, ShippingStrategy shippingStrategy) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.shippingStrategy = Objects.requireNonNull(shippingStrategy, "shippingStrategy");
    }

    public OrderId placeOrder(List<OrderLine> lines) {
        Order order = new Order(OrderId.newId(), lines);
        repository.save(order);
        return order.id();
    }

    /** Total order cost = sum of line subtotals + shipping (strategy-driven). */
    public Money totalWithShipping(OrderId id) {
        Order order = require(id);
        return order.total().add(shippingStrategy.cost(order));
    }

    public void payOrder(OrderId id) {
        mutate(id, Order::pay);
    }

    public void shipOrder(OrderId id) {
        mutate(id, Order::ship);
    }

    public void cancelOrder(OrderId id) {
        mutate(id, Order::cancel);
    }

    /**
     * Run a state-changing action on an order under its dedicated lock, then persist.
     * The whole load-mutate-save sequence is serialized per order id.
     */
    private void mutate(OrderId id, java.util.function.Consumer<Order> action) {
        Lock lock = locks.computeIfAbsent(id, k -> new ReentrantLock());
        lock.lock();
        try {
            Order order = require(id);
            action.accept(order);     // delegates the rule to the entity; may throw IllegalOrderStateException
            repository.save(order);
        } finally {
            lock.unlock();
        }
    }

    private Order require(OrderId id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No order: " + id.value()));
    }
}
