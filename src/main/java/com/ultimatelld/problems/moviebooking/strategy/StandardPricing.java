package com.ultimatelld.problems.moviebooking.strategy;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.moviebooking.entity.SeatTier;

/** Flat per-tier pricing. */
public final class StandardPricing implements SeatPricing {
    @Override
    public Money priceFor(SeatTier tier) {
        return switch (tier) {
            case SILVER -> Money.of(150_00);
            case GOLD -> Money.of(250_00);
            case PLATINUM -> Money.of(400_00);
        };
    }
}
