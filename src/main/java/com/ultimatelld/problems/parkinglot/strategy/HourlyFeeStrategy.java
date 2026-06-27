package com.ultimatelld.problems.parkinglot.strategy;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.parkinglot.entity.VehicleType;

/** Per-hour pricing by vehicle type; any partial hour is rounded up, with a 1-hour minimum. */
public final class HourlyFeeStrategy implements FeeStrategy {

    private static final long MILLIS_PER_HOUR = 3_600_000L;

    @Override
    public Money fee(VehicleType type, long durationMillis) {
        long hours = Math.max(1, (durationMillis + MILLIS_PER_HOUR - 1) / MILLIS_PER_HOUR);
        long perHourMinor = switch (type) {
            case MOTORCYCLE -> 20_00;
            case CAR -> 40_00;
            case TRUCK -> 80_00;
        };
        return Money.of(perHourMinor).multiply((int) hours);
    }
}
