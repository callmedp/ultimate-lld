package com.ultimatelld.theory.module02patterns.behavioral;

import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * OBSERVER pattern — the THREAD-SAFE publisher.
 * <p>
 * The subscriber list is a {@link CopyOnWriteArrayList}: iteration during {@link #publish} never
 * throws {@code ConcurrentModificationException} even if observers subscribe/unsubscribe
 * concurrently, and publishing threads never block each other. COW is ideal here because the
 * list is read (published to) far more often than it is mutated (subscribe/unsubscribe).
 * <p>
 * A faulty observer is isolated: its exception is swallowed (and counted) so one bad subscriber
 * cannot break delivery to the others.
 */
public final class Subject {

    private final CopyOnWriteArrayList<Observer> observers = new CopyOnWriteArrayList<>();
    private final AtomicLong deliveries = new AtomicLong();
    private final AtomicLong failures = new AtomicLong();

    public void subscribe(Observer observer) {
        observers.addIfAbsent(Objects.requireNonNull(observer, "observer"));
    }

    public void unsubscribe(Observer observer) {
        observers.remove(observer);
    }

    public void publish(String event) {
        Objects.requireNonNull(event, "event");
        for (Observer o : observers) {
            try {
                o.onEvent(event);
                deliveries.incrementAndGet();
            } catch (RuntimeException e) {
                failures.incrementAndGet(); // isolate a misbehaving subscriber
            }
        }
    }

    public int subscriberCount() {
        return observers.size();
    }

    public long deliveryCount() {
        return deliveries.get();
    }

    public long failureCount() {
        return failures.get();
    }
}
