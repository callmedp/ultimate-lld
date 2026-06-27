package com.ultimatelld.theory.module02patterns.behavioral;

/**
 * OBSERVER pattern — the subscriber side. Implementations react to events published by a
 * {@link Subject}. Decoupling: the subject knows only this interface, never concrete observers.
 */
public interface Observer {

    void onEvent(String event);
}
