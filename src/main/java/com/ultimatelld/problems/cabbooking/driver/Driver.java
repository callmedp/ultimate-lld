package com.ultimatelld.problems.cabbooking.driver;

import com.ultimatelld.problems.cabbooking.entity.CabDriver;
import com.ultimatelld.problems.cabbooking.entity.Location;
import com.ultimatelld.problems.cabbooking.entity.Rider;
import com.ultimatelld.problems.cabbooking.entity.Trip;
import com.ultimatelld.problems.cabbooking.exception.NoDriverAvailableException;
import com.ultimatelld.problems.cabbooking.service.RideService;
import com.ultimatelld.problems.cabbooking.strategy.DistanceFareStrategy;
import com.ultimatelld.problems.cabbooking.strategy.NearestDriverStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Composition root + concurrent ride-matching simulation.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        List<CabDriver> fleet = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            fleet.add(new CabDriver("driver-" + i, new Location(i * 10, i * 10)));
        }
        RideService service = new RideService(fleet, new NearestDriverStrategy(),
                new DistanceFareStrategy(50_00, 12_00));  // base 50.00 + 12.00/unit

        // ---- 1. 20 riders request simultaneously; only 5 drivers -> 5 matched, no double-assign ----
        int riders = 20;
        ExecutorService pool = Executors.newFixedThreadPool(riders);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(riders);
        CopyOnWriteArrayList<Trip> trips = new CopyOnWriteArrayList<>();
        AtomicInteger rejected = new AtomicInteger();

        for (int i = 0; i < riders; i++) {
            Rider rider = new Rider("rider-" + i);
            pool.submit(() -> {
                try {
                    start.await();
                    Location pickup = new Location(
                            ThreadLocalRandom.current().nextInt(0, 50),
                            ThreadLocalRandom.current().nextInt(0, 50));
                    trips.add(service.requestRide(rider, pickup));
                } catch (NoDriverAvailableException e) {
                    rejected.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        pool.shutdownNow();

        long distinctDrivers = trips.stream().map(Trip::driverId).distinct().count();
        System.out.println("20 concurrent ride requests, 5 drivers -> matched=" + trips.size()
                + ", rejected=" + rejected.get() + " (expected 5 / 15)");
        System.out.println("distinct drivers assigned = " + distinctDrivers
                + " (must equal matched count -> no driver double-booked)");
        System.out.println("available drivers now = " + service.availableDrivers()
                + ", active trips = " + service.activeTrips());

        // ---- 2. End a trip -> its driver frees up -> a new rider can be matched ----
        Trip first = trips.get(0);
        service.endTrip(first.id());
        System.out.println("ended trip on " + first.driverId() + " (fare was " + first.fare() + ")");
        Trip late = service.requestRide(new Rider("rider-late"), new Location(5, 5));
        System.out.println("late rider matched to " + late.driverId() + ", fare=" + late.fare());
        System.out.println("active trips = " + service.activeTrips() + ", available = " + service.availableDrivers());
    }
}
