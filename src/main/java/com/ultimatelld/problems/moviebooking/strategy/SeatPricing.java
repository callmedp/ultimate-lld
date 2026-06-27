package com.ultimatelld.problems.moviebooking.strategy;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.moviebooking.entity.SeatTier;

/** Pricing strategy (OCP): new pricing schemes (surge, weekday) are new implementations. */
public interface SeatPricing {
    Money priceFor(SeatTier tier);
}
