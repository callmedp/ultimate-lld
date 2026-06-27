package com.ultimatelld.problems.parkinglot.strategy;

import com.ultimatelld.problems.parkinglot.entity.ParkingSpot;
import com.ultimatelld.problems.parkinglot.entity.Vehicle;

import java.util.Comparator;
import java.util.List;

/**
 * Prefers the SMALLEST spot that still fits the vehicle, preserving larger spots for larger
 * vehicles (reduces the chance a truck is later turned away because cars took the LARGE spots).
 */
public final class BestFitStrategy implements SpotAllocationStrategy {
    @Override
    public List<ParkingSpot> orderCandidates(List<ParkingSpot> fittingSpots, Vehicle vehicle) {
        return fittingSpots.stream()
                .sorted(Comparator.comparingInt((ParkingSpot s) -> s.size().ordinal())
                        .thenComparingInt(ParkingSpot::floor)
                        .thenComparingInt(ParkingSpot::index))
                .toList();
    }

    @Override
    public String name() {
        return "BEST_FIT";
    }
}
