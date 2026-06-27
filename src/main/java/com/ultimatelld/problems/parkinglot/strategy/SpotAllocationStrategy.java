package com.ultimatelld.problems.parkinglot.strategy;

import com.ultimatelld.problems.parkinglot.entity.ParkingSpot;
import com.ultimatelld.problems.parkinglot.entity.Vehicle;

import java.util.List;

/**
 * OCP allocation policy. Given the fitting spots for a vehicle, return them in the ORDER the
 * service should attempt to claim them. The service performs the atomic claim, so a strategy is a
 * pure ordering function — adding NearestFirst, BestFit, RandomBalanced, etc. needs zero core edits.
 */
public interface SpotAllocationStrategy {
    List<ParkingSpot> orderCandidates(List<ParkingSpot> fittingSpots, Vehicle vehicle);

    String name();
}
