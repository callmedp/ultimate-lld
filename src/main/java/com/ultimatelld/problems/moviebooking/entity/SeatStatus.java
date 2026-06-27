package com.ultimatelld.problems.moviebooking.entity;

/** Seat lifecycle within a show: AVAILABLE -> HELD -> BOOKED, with HELD -> AVAILABLE on expiry. */
public enum SeatStatus {
    AVAILABLE,
    HELD,
    BOOKED
}
