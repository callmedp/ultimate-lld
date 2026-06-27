package com.ultimatelld.common;

import java.util.Objects;

/**
 * Immutable money value object stored in MINOR units (paise / cents).
 * <p>
 * Interview rule: never represent money as {@code double} — floating point rounding
 * silently corrupts financial math. Store the smallest indivisible unit as a {@code long}.
 */
public final class Money {

    public static final Money ZERO = new Money(0L);

    private final long minor;

    private Money(long minor) {
        this.minor = minor;
    }

    public static Money of(long minor) {
        return new Money(minor);
    }

    public long minor() {
        return minor;
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "other");
        return new Money(Math.addExact(this.minor, other.minor));
    }

    public Money multiply(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("quantity must be >= 0");
        return new Money(Math.multiplyExact(this.minor, quantity));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money money)) return false;
        return minor == money.minor;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(minor);
    }

    @Override
    public String toString() {
        return String.format("%d.%02d", minor / 100, Math.abs(minor % 100));
    }
}
