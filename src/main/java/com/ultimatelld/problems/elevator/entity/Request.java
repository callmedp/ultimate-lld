package com.ultimatelld.problems.elevator.entity;

/**
 * A hall call: a passenger at {@code floor} wants to travel in {@code direction}.
 * (A car call — pressing a floor button inside the car — is modelled the same way as a stop.)
 */
public record Request(int floor, Direction direction) {
    public Request {
        if (floor < 0) throw new IllegalArgumentException("floor must be >= 0");
        if (direction == Direction.IDLE) throw new IllegalArgumentException("a request must have a direction");
    }
}
