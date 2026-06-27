package com.ultimatelld.problems.elevator.strategy;

import com.ultimatelld.problems.elevator.entity.Elevator;
import com.ultimatelld.problems.elevator.entity.Request;

import java.util.List;

/** OCP dispatch policy: which car should serve a hall call. New policies are new classes. */
public interface DispatchStrategy {
    Elevator selectElevator(List<Elevator> elevators, Request request);

    String name();
}
