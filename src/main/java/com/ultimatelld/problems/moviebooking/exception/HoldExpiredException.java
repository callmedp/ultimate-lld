package com.ultimatelld.problems.moviebooking.exception;

import com.ultimatelld.problems.moviebooking.entity.Ids.HoldId;

/** Thrown when confirming against a hold that has expired or no longer exists. */
public class HoldExpiredException extends RuntimeException {
    public HoldExpiredException(HoldId holdId) {
        super("Hold expired or unknown: " + holdId.value());
    }
}
