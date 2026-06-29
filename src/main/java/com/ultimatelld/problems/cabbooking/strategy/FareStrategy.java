package com.ultimatelld.problems.cabbooking.strategy;

import com.ultimatelld.common.Money;

/** OCP fare policy: flat+distance, surge, time-of-day — each a separate implementation. */
public interface FareStrategy {
    Money quote(double distanceUnits);
}
