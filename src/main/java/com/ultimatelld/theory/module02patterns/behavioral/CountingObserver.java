package com.ultimatelld.theory.module02patterns.behavioral;

import java.util.concurrent.atomic.AtomicLong;

/**
 * OBSERVER — a concrete subscriber that atomically counts the events it receives, so the driver
 * can assert that concurrent publishes were all delivered correctly.
 */
public final class CountingObserver implements Observer {

    private final String id;
    private final AtomicLong received = new AtomicLong();

    public CountingObserver(String id) {
        this.id = java.util.Objects.requireNonNull(id, "id");
    }

    @Override
    public void onEvent(String event) {
        received.incrementAndGet();
    }

    public String id() {
        return id;
    }

    public long received() {
        return received.get();
    }
}
