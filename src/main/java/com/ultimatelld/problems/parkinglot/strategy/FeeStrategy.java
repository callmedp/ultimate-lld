package com.ultimatelld.problems.parkinglot.strategy;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.parkinglot.entity.VehicleType;

/** OCP fee policy: hourly, tiered, weekday/weekend — each a separate implementation. */
public interface FeeStrategy {
    Money fee(VehicleType type, long durationMillis);
}
