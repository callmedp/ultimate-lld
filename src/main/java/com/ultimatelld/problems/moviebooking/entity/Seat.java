package com.ultimatelld.problems.moviebooking.entity;

import com.ultimatelld.problems.moviebooking.entity.Ids.SeatId;

import java.util.Objects;

/** Immutable seat definition (identity + tier). Its mutable status lives inside the Show. */
public record Seat(SeatId id, SeatTier tier) {
    public Seat {
        Objects.requireNonNull(id);
        Objects.requireNonNull(tier);
    }
}
