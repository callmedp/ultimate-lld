package com.ultimatelld.problems.elevator.strategy;

import com.ultimatelld.problems.elevator.entity.Elevator;
import com.ultimatelld.problems.elevator.entity.Request;

import java.util.Comparator;
import java.util.List;

/** Balances load by assigning the car with the fewest pending stops. */
public final class LeastBusyStrategy implements DispatchStrategy {
    @Override
    public Elevator selectElevator(List<Elevator> elevators, Request request) {
        return elevators.stream()
                .min(Comparator
                        .comparingInt(Elevator::pendingStops)
                        .thenComparingInt(e -> Math.abs(e.currentFloor() - request.floor())))
                .orElseThrow(() -> new IllegalStateException("no elevators configured"));
    }

    @Override
    public String name() {
        return "LEAST_BUSY";
    }
}
