package com.ultimatelld.problems.elevator.service;

import com.ultimatelld.problems.elevator.entity.Elevator;
import com.ultimatelld.problems.elevator.entity.Request;
import com.ultimatelld.problems.elevator.strategy.DispatchStrategy;

import java.util.List;
import java.util.Objects;

/**
 * Coordinates a bank of elevators. {@link #submitRequest} is safe to call from many threads at once
 * (each car serializes its own stop list); the dispatch decision is injected via {@link DispatchStrategy}.
 * {@link #step} advances every car one tick — a real deployment would instead run each car on its own
 * thread/timer, which this design already supports since cars are independently lockable.
 */
public final class ElevatorController {

    private final List<Elevator> elevators;
    private final DispatchStrategy dispatchStrategy;

    public ElevatorController(List<Elevator> elevators, DispatchStrategy dispatchStrategy) {
        if (elevators.isEmpty()) throw new IllegalArgumentException("need at least one elevator");
        this.elevators = List.copyOf(elevators);
        this.dispatchStrategy = Objects.requireNonNull(dispatchStrategy);
    }

    public int submitRequest(Request request) {
        Elevator chosen = dispatchStrategy.selectElevator(elevators, request);
        chosen.addStop(request.floor());
        return chosen.id();
    }

    /** Advances all cars by one tick; returns the number of stops served this tick. */
    public int step() {
        int served = 0;
        for (Elevator e : elevators) {
            if (e.step()) served++;
        }
        return served;
    }

    public boolean allIdle() {
        return elevators.stream().allMatch(Elevator::isIdle);
    }

    public List<Elevator> elevators() {
        return elevators;
    }
}
