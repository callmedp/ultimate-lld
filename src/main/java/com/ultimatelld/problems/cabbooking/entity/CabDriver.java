package com.ultimatelld.problems.cabbooking.entity;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A driver/cab resource. Assignment is a lock-free atomic transition: {@link #tryReserve} uses
 * compare-and-set on the status, so when several riders race for the same nearby driver EXACTLY ONE
 * wins and the others fall through to the next candidate — no driver is ever double-booked.
 */
public final class CabDriver {

    private final String id;
    private final AtomicReference<DriverStatus> status = new AtomicReference<>(DriverStatus.AVAILABLE);
    private volatile Location location;

    public CabDriver(String id, Location location) {
        this.id = Objects.requireNonNull(id);
        this.location = Objects.requireNonNull(location);
    }

    public String id() {
        return id;
    }

    public Location location() {
        return location;
    }

    public void updateLocation(Location location) {
        this.location = Objects.requireNonNull(location);
    }

    public boolean isAvailable() {
        return status.get() == DriverStatus.AVAILABLE;
    }

    /** @return true if this caller claimed the driver; false if already on a trip. */
    public boolean tryReserve() {
        return status.compareAndSet(DriverStatus.AVAILABLE, DriverStatus.ON_TRIP);
    }

    public void release() {
        status.set(DriverStatus.AVAILABLE);
    }
}
