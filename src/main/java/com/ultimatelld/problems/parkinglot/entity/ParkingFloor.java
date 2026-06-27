package com.ultimatelld.problems.parkinglot.entity;

import java.util.List;
import java.util.Objects;

/** A floor holding an immutable set of spots (the spots themselves carry mutable occupancy). */
public final class ParkingFloor {

    private final int number;
    private final List<ParkingSpot> spots;

    public ParkingFloor(int number, List<ParkingSpot> spots) {
        this.number = number;
        this.spots = List.copyOf(Objects.requireNonNull(spots));
    }

    public int number() {
        return number;
    }

    public List<ParkingSpot> spots() {
        return spots;
    }
}
