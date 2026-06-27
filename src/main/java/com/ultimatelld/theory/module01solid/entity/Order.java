package com.ultimatelld.theory.module01solid.entity;

import com.ultimatelld.common.Money;
import com.ultimatelld.theory.module01solid.exception.IllegalOrderStateException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RICH domain entity — the opposite of an anemic data bag.
 * <p>
 * The Order is the guardian of its own invariants:
 * <ul>
 *   <li>It can never exist without an id and at least one line (enforced in the constructor).</li>
 *   <li>State transitions go through {@link #pay()}, {@link #ship()}, {@link #cancel()} —
 *       there is no public setter for status, so no caller can force an illegal state.</li>
 *   <li>It exposes its lines only as a defensive copy, so external code cannot mutate internals.</li>
 * </ul>
 * Note: this class is intentionally NOT thread-safe. Synchronization is the Service layer's job
 * (see Module 3). A domain entity guarding business invariants and a service guarding concurrency
 * are two distinct responsibilities (SRP).
 */
public final class Order {

    private final OrderId id;
    private final List<OrderLine> lines;
    private OrderStatus status;

    public Order(OrderId id, List<OrderLine> lines) {
        this.id = Objects.requireNonNull(id, "id");
        Objects.requireNonNull(lines, "lines");
        if (lines.isEmpty()) throw new IllegalArgumentException("an order needs at least one line");
        this.lines = new ArrayList<>(lines);   // copy in — caller can't mutate our state later
        this.status = OrderStatus.CREATED;
    }

    public OrderId id() {
        return id;
    }

    public OrderStatus status() {
        return status;
    }

    /** Defensive copy out — callers receive a read-only snapshot, never the live list. */
    public List<OrderLine> lines() {
        return List.copyOf(lines);
    }

    /** Behavior lives on the entity: the order knows how to total itself. */
    public Money total() {
        return lines.stream()
                .map(OrderLine::subtotal)
                .reduce(Money.ZERO, Money::add);
    }

    public void pay() {
        transitionTo(OrderStatus.PAID);
    }

    public void ship() {
        transitionTo(OrderStatus.SHIPPED);
    }

    public void deliver() {
        transitionTo(OrderStatus.DELIVERED);
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
    }

    private void transitionTo(OrderStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalOrderStateException(status, target);
        }
        this.status = target;
    }

    @Override
    public String toString() {
        return "Order{id=" + id.value() + ", status=" + status + ", total=" + total() + "}";
    }
}
