package com.ultimatelld.problems.elevator.strategy;

import com.ultimatelld.problems.elevator.entity.Elevator;
import com.ultimatelld.problems.elevator.entity.Request;

import java.util.Comparator;
import java.util.List;

/**
 * Assigns the car closest to the request floor, breaking ties toward the less-loaded car.
 * (A production "elevator algorithm" would also weigh current direction vs. the call direction.)
 */
public final class NearestCarStrategy implements DispatchStrategy {
    @Override
    public Elevator selectElevator(List<Elevator> elevators, Request request) {
        return elevators.stream()
                .min(Comparator
                        .comparingInt((Elevator e) -> Math.abs(e.currentFloor() - request.floor()))
                        .thenComparingInt(Elevator::pendingStops))
                .orElseThrow(() -> new IllegalStateException("no elevators configured"));
    }

    @Override
    public String name() {
        return "NEAREST_CAR";
    }
}
