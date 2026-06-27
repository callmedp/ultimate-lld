package com.ultimatelld.problems.parkinglot.entity;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Vehicle types and the spot sizes each may occupy. Adding a new type (e.g. ELECTRIC) is a new
 * enum constant with its allowed sizes — no allocation/fee code changes.
 */
public enum VehicleType {
    MOTORCYCLE(EnumSet.of(SpotSize.SMALL, SpotSize.MEDIUM, SpotSize.LARGE)),
    CAR(EnumSet.of(SpotSize.MEDIUM, SpotSize.LARGE)),
    TRUCK(EnumSet.of(SpotSize.LARGE));

    private final Set<SpotSize> allowedSizes;

    VehicleType(Set<SpotSize> allowedSizes) {
        this.allowedSizes = Collections.unmodifiableSet(allowedSizes);
    }

    public Set<SpotSize> allowedSizes() {
        return allowedSizes;
    }
}
