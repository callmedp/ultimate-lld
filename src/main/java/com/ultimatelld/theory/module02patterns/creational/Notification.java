package com.ultimatelld.theory.module02patterns.creational;

/**
 * FACTORY pattern — product interface.
 * <p>
 * Callers depend on this abstraction, never on the concrete {@code EmailNotification} /
 * {@code SmsNotification} / {@code PushNotification}. Adding a new channel = a new class +
 * one line in the factory; no caller changes (OCP).
 */
public interface Notification {

    /** @return a human-readable transcript of what was dispatched (so the driver can print it). */
    String send(String recipient, String message);

    /** The channel this notification travels over. */
    Channel channel();
}
