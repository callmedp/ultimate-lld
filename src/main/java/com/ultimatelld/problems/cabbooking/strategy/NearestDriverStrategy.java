package com.ultimatelld.problems.cabbooking.strategy;

import com.ultimatelld.problems.cabbooking.entity.CabDriver;
import com.ultimatelld.problems.cabbooking.entity.Location;

import java.util.Comparator;
import java.util.List;

/** Ranks available drivers by proximity to the pickup. */
public final class NearestDriverStrategy implements DriverMatchingStrategy {
    @Override
    public List<CabDriver> rank(List<CabDriver> availableDrivers, Location pickup) {
        return availableDrivers.stream()
                .sorted(Comparator.comparingDouble(d -> d.location().distanceTo(pickup)))
                .toList();
    }

    @Override
    public String name() {
        return "NEAREST_DRIVER";
    }
}
