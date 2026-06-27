package com.ultimatelld.theory.module02patterns.creational;

/**
 * SINGLETON — ENUM idiom (recommended, especially when serialization is involved).
 * <p>
 * A single-element enum is the most concise thread-safe singleton: the JVM guarantees exactly
 * one instance, and it is the only form that is inherently safe against reflection attacks and
 * serialization (which can otherwise be used to mint a second instance of a class-based singleton).
 */
public enum EnumSingleton {
    INSTANCE;

    public String describe() {
        return "EnumSingleton@" + Integer.toHexString(System.identityHashCode(this));
    }
}
