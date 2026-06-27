package com.ultimatelld.problems.parkinglot.entity;

import com.ultimatelld.problems.parkinglot.entity.Ids.VehicleId;

import java.util.Objects;

/** Immutable vehicle. */
public record Vehicle(VehicleId id, VehicleType type) {
    public Vehicle {
        Objects.requireNonNull(id);
        Objects.requireNonNull(type);
    }
}
