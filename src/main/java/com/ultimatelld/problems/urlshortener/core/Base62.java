package com.ultimatelld.problems.urlshortener.core;

/** Encodes a non-negative long into a compact base-62 string ([0-9a-zA-Z]). */
public final class Base62 {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALPHABET.length();

    private Base62() {
    }

    public static String encode(long value) {
        if (value < 0) throw new IllegalArgumentException("value must be >= 0");
        if (value == 0) return "0";
        StringBuilder sb = new StringBuilder();
        long v = value;
        while (v > 0) {
            sb.append(ALPHABET.charAt((int) (v % BASE)));
            v /= BASE;
        }
        return sb.reverse().toString();
    }
}
