package com.ultimatelld.problems.parkinglot.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** The lot: an ordered collection of floors. Exposes the flat list of spots for allocation. */
public final class ParkingLot {

    private final String name;
    private final List<ParkingFloor> floors;
    private final List<ParkingSpot> allSpots;

    public ParkingLot(String name, List<ParkingFloor> floors) {
        this.name = Objects.requireNonNull(name);
        this.floors = List.copyOf(Objects.requireNonNull(floors));
        List<ParkingSpot> flat = new ArrayList<>();
        for (ParkingFloor f : this.floors) {
            flat.addAll(f.spots());
        }
        this.allSpots = List.copyOf(flat);
    }

    public String name() {
        return name;
    }

    public List<ParkingFloor> floors() {
        return floors;
    }

    public List<ParkingSpot> allSpots() {
        return allSpots;
    }
}
