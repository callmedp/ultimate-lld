package com.ultimatelld.theory.module02patterns.creational;

/**
 * SINGLETON — DOUBLE-CHECKED LOCKING idiom.
 * <p>
 * Shown for completeness and interview value. The {@code volatile} keyword is NON-NEGOTIABLE:
 * without it, the publishing thread's partially-constructed object could be observed by another
 * thread due to instruction reordering (the reference is assigned before the constructor's writes
 * are visible). The first (unlocked) check avoids paying for the lock on the common path; the
 * second (locked) check guards against two threads racing past the first check.
 * <p>
 * In practice prefer {@link HolderSingleton} — it is simpler and just as correct.
 */
public final class DoubleCheckedSingleton {

    private static volatile DoubleCheckedSingleton instance;

    private DoubleCheckedSingleton() {
    }

    public static DoubleCheckedSingleton getInstance() {
        DoubleCheckedSingleton local = instance; // read volatile once into a local (perf)
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

    public String describe() {
        return "DoubleCheckedSingleton@" + Integer.toHexString(System.identityHashCode(this));
    }
}
