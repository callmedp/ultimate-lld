package com.ultimatelld.problems.cabbooking.strategy;

import com.ultimatelld.common.Money;

/** Base fare plus a per-distance-unit charge. */
public final class DistanceFareStrategy implements FareStrategy {

    private final long baseMinor;
    private final long perUnitMinor;

    public DistanceFareStrategy(long baseMinor, long perUnitMinor) {
        this.baseMinor = baseMinor;
        this.perUnitMinor = perUnitMinor;
    }

    @Override
    public Money quote(double distanceUnits) {
        long fare = baseMinor + Math.round(perUnitMinor * Math.max(0, distanceUnits));
        return Money.of(fare);
    }
}
