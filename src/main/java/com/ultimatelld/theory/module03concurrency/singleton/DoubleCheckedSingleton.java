package com.ultimatelld.theory.module03concurrency.singleton;

/**
 * Double-checked locking. Correct ONLY because {@code instance} is {@code volatile} — without it,
 * a partially-constructed object could be published due to instruction reordering. Shown for
 * completeness; in modern Java prefer {@link HolderSingleton} or an enum (less ceremony, no bug surface).
 */
public final class DoubleCheckedSingleton {

    private static volatile DoubleCheckedSingleton instance;

    private DoubleCheckedSingleton() {
    }

    public static DoubleCheckedSingleton getInstance() {
        DoubleCheckedSingleton local = instance;   // read volatile once for performance
        if (local == null) {
            synchronized (DoubleCheckedSingleton.class) {
                local = instance;
                if (local == null) {
                    local = new DoubleCheckedSingleton();
                    instance = local;
                }
            }
        }
        return local;
    }
}
