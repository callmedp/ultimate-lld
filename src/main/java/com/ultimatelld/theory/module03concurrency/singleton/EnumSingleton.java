package com.ultimatelld.theory.module03concurrency.singleton;

/**
 * Enum singleton — the simplest fully-safe option. The JVM guarantees a single instance, handles
 * thread-safety at class-init, and it is serialization- and reflection-proof for free.
 */
public enum EnumSingleton {
    INSTANCE;

    public String greet() {
        return "single enum instance @" + Integer.toHexString(System.identityHashCode(this));
    }
}
