package com.ultimatelld.theory.module02patterns.creational;

import java.util.Objects;

/**
 * FACTORY pattern — centralizes the {@code switch} over a product family so callers never
 * {@code new} a concrete {@link Notification}. The creation knowledge lives in exactly one
 * place; the rest of the system programs to the {@link Notification} interface (DIP).
 * <p>
 * Why a factory and not a raw {@code switch} at every call site: a new channel is added by
 * editing only this class, and the dependency on concretes is quarantined here.
 */
public final class NotificationFactory {

    /**
     * @throws NullPointerException if channel is null.
     */
    public Notification create(Channel channel) {
        Objects.requireNonNull(channel, "channel");
        return switch (channel) {
            case EMAIL -> new EmailNotification();
            case SMS -> new SmsNotification();
            case PUSH -> new PushNotification();
        };
    }
}
