package com.ultimatelld.problems.moviebooking.exception;

import com.ultimatelld.problems.moviebooking.entity.Ids.SeatId;

/** Thrown when a requested seat is not AVAILABLE at lock time (already held or booked). */
public class SeatUnavailableException extends RuntimeException {
    public SeatUnavailableException(SeatId seatId) {
        super("Seat not available: " + seatId.value());
    }
}
