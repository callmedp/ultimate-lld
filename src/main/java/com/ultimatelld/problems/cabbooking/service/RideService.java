package com.ultimatelld.problems.cabbooking.service;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.cabbooking.entity.CabDriver;
import com.ultimatelld.problems.cabbooking.entity.Location;
import com.ultimatelld.problems.cabbooking.entity.Rider;
import com.ultimatelld.problems.cabbooking.entity.Trip;
import com.ultimatelld.problems.cabbooking.exception.NoDriverAvailableException;
import com.ultimatelld.problems.cabbooking.strategy.DriverMatchingStrategy;
import com.ultimatelld.problems.cabbooking.strategy.FareStrategy;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Matches riders to drivers. Lock-free under contention: the {@link DriverMatchingStrategy} ranks a
 * snapshot of available drivers, then the service walks that ranking attempting an atomic
 * {@link CabDriver#tryReserve()} (CAS). The first reservation that succeeds wins; if a driver was
 * grabbed by a concurrent request, its CAS fails and we move to the next candidate. No driver is
 * ever assigned to two riders.
 */
public final class RideService {

    private final List<CabDriver> drivers;
    private final DriverMatchingStrategy matchingStrategy;
    private final FareStrategy fareStrategy;
    private final ConcurrentHashMap<String, CabDriver> driverByTrip = new ConcurrentHashMap<>();

    public RideService(List<CabDriver> drivers, DriverMatchingStrategy matchingStrategy, FareStrategy fareStrategy) {
        if (drivers.isEmpty()) throw new IllegalArgumentException("need at least one driver");
        this.drivers = List.copyOf(drivers);
        this.matchingStrategy = Objects.requireNonNull(matchingStrategy);
        this.fareStrategy = Objects.requireNonNull(fareStrategy);
    }

    public Trip requestRide(Rider rider, Location pickup) {
        List<CabDriver> available = drivers.stream().filter(CabDriver::isAvailable).toList();
        for (CabDriver candidate : matchingStrategy.rank(available, pickup)) {
            if (candidate.tryReserve()) {
                Money fare = fareStrategy.quote(candidate.location().distanceTo(pickup));
                Trip trip = Trip.create(candidate.id(), rider.id(), pickup, fare);
                driverByTrip.put(trip.id(), candidate);
                return trip;
            }
        }
        throw new NoDriverAvailableException();
    }

    /** Completes a trip, returning the driver to the available pool. */
    public void endTrip(String tripId) {
        CabDriver driver = driverByTrip.remove(tripId);
        if (driver == null) throw new NoSuchElementException("no active trip: " + tripId);
        driver.release();
    }

    public long availableDrivers() {
        return drivers.stream().filter(CabDriver::isAvailable).count();
    }

    public int activeTrips() {
        return driverByTrip.size();
    }
}
