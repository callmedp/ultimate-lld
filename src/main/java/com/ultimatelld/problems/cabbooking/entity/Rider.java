package com.ultimatelld.problems.cabbooking.entity;

import java.util.Objects;

/** A rider requesting trips. */
public record Rider(String id) {
    public Rider {
        Objects.requireNonNull(id);
    }
}
