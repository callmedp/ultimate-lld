package com.ultimatelld.problems.parkinglot.entity;

import com.ultimatelld.problems.parkinglot.entity.Ids.SpotId;
import com.ultimatelld.problems.parkinglot.entity.Ids.VehicleId;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A parking spot. Occupancy is a lock-free atomic transition: {@link #tryOccupy} uses
 * compare-and-set so that when two threads race for the same free spot, EXACTLY ONE wins and
 * the other observes the spot as taken — no double allocation, no lock required.
 */
public final class ParkingSpot {

    private final SpotId id;
    private final SpotSize size;
    private final int floor;
    private final int index;
    private final AtomicReference<VehicleId> occupant = new AtomicReference<>(null);

    public ParkingSpot(SpotId id, SpotSize size, int floor, int index) {
        this.id = Objects.requireNonNull(id);
        this.size = Objects.requireNonNull(size);
        this.floor = floor;
        this.index = index;
    }

    public SpotId id() {
        return id;
    }

    public SpotSize size() {
        return size;
    }

    public int floor() {
        return floor;
    }

    public int index() {
        return index;
    }

    public boolean isFree() {
        return occupant.get() == null;
    }

    public boolean canFit(VehicleType type) {
        return size.fits(type);
    }

    /** @return true if this thread claimed the spot; false if another vehicle holds it. */
    public boolean tryOccupy(VehicleId vehicleId) {
        return occupant.compareAndSet(null, vehicleId);
    }

    /** Frees the spot only if the given vehicle currently holds it (guards against stale releases). */
    public boolean vacate(VehicleId vehicleId) {
        return occupant.compareAndSet(vehicleId, null);
    }
}
