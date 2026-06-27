package com.ultimatelld.problems.parkinglot.entity;

/** Physical spot sizes. A spot fits a vehicle iff the vehicle type permits this size. */
public enum SpotSize {
    SMALL,
    MEDIUM,
    LARGE;

    public boolean fits(VehicleType type) {
        return type.allowedSizes().contains(this);
    }
}
