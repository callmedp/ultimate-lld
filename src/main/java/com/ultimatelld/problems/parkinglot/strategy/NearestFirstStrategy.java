package com.ultimatelld.problems.parkinglot.strategy;

import com.ultimatelld.problems.parkinglot.entity.ParkingSpot;
import com.ultimatelld.problems.parkinglot.entity.Vehicle;

import java.util.Comparator;
import java.util.List;

/** Prefers the lowest floor, then the lowest index — i.e. closest to the entrance. */
public final class NearestFirstStrategy implements SpotAllocationStrategy {
    @Override
    public List<ParkingSpot> orderCandidates(List<ParkingSpot> fittingSpots, Vehicle vehicle) {
        return fittingSpots.stream()
                .sorted(Comparator.comparingInt(ParkingSpot::floor).thenComparingInt(ParkingSpot::index))
                .toList();
    }

    @Override
    public String name() {
        return "NEAREST_FIRST";
    }
}
