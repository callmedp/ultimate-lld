package com.ultimatelld.theory.module02patterns.creational;

/**
 * SINGLETON — the INITIALIZATION-ON-DEMAND HOLDER idiom (recommended).
 * <p>
 * The nested {@code Holder} class is not loaded until {@link #getInstance()} is first called,
 * giving lazy initialization. The JVM guarantees class initialization is thread-safe, so we get
 * correct lazy singletons with ZERO synchronization in our own code — no locks, no volatile.
 */
public final class HolderSingleton {

    private HolderSingleton() {
    }

    private static final class Holder {
        private static final HolderSingleton INSTANCE = new HolderSingleton();
    }

    public static HolderSingleton getInstance() {
        return Holder.INSTANCE;
    }

    public String describe() {
        return "HolderSingleton@" + Integer.toHexString(System.identityHashCode(this));
    }
}
