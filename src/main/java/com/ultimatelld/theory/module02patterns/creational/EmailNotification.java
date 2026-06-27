package com.ultimatelld.theory.module02patterns.creational;

import java.util.Objects;

/** Concrete product. */
public final class EmailNotification implements Notification {

    @Override
    public String send(String recipient, String message) {
        Objects.requireNonNull(recipient, "recipient");
        Objects.requireNonNull(message, "message");
        return "EMAIL to <" + recipient + ">: " + message;
    }

    @Override
    public Channel channel() {
        return Channel.EMAIL;
    }
}
