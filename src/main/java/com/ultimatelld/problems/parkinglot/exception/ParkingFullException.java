package com.ultimatelld.problems.parkinglot.exception;

import com.ultimatelld.problems.parkinglot.entity.VehicleType;

/** Thrown when no compatible free spot exists for the vehicle. */
public class ParkingFullException extends RuntimeException {
    public ParkingFullException(VehicleType type) {
        super("No free spot for vehicle type: " + type);
    }
}
