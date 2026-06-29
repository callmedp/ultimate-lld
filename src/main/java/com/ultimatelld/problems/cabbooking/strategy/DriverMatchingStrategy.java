package com.ultimatelld.problems.cabbooking.strategy;

import com.ultimatelld.problems.cabbooking.entity.CabDriver;
import com.ultimatelld.problems.cabbooking.entity.Location;

import java.util.List;

/**
 * OCP matching policy: orders candidate drivers for a pickup. The service does the atomic
 * reservation, so a strategy is a pure ranking function (nearest, highest-rated, surge-aware, ...).
 */
public interface DriverMatchingStrategy {
    List<CabDriver> rank(List<CabDriver> availableDrivers, Location pickup);

    String name();
}
