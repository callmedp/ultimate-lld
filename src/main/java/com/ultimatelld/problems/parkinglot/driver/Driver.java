package com.ultimatelld.problems.parkinglot.driver;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.parkinglot.entity.Ids.SpotId;
import com.ultimatelld.problems.parkinglot.entity.Ids.VehicleId;
import com.ultimatelld.problems.parkinglot.entity.ParkingFloor;
import com.ultimatelld.problems.parkinglot.entity.ParkingLot;
import com.ultimatelld.problems.parkinglot.entity.ParkingSpot;
import com.ultimatelld.problems.parkinglot.entity.SpotSize;
import com.ultimatelld.problems.parkinglot.entity.Ticket;
import com.ultimatelld.problems.parkinglot.entity.Vehicle;
import com.ultimatelld.problems.parkinglot.entity.VehicleType;
import com.ultimatelld.problems.parkinglot.exception.ParkingFullException;
import com.ultimatelld.problems.parkinglot.repository.InMemoryTicketRepository;
import com.ultimatelld.problems.parkinglot.repository.TicketRepository;
import com.ultimatelld.problems.parkinglot.service.ParkingLotService;
import com.ultimatelld.problems.parkinglot.strategy.BestFitStrategy;
import com.ultimatelld.problems.parkinglot.strategy.HourlyFeeStrategy;
import com.ultimatelld.problems.parkinglot.util.Clock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Composition root + concurrent entry/exit simulation for the parking lot.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        // Lot: 1 floor, 4 SMALL + 3 MEDIUM + 2 LARGE = 9 spots. Cars fit MEDIUM+LARGE = 5 spots.
        List<ParkingSpot> spots = new ArrayList<>();
        int idx = 0;
        for (int i = 0; i < 4; i++) spots.add(new ParkingSpot(SpotId.of("SM-" + i), SpotSize.SMALL, 0, idx++));
        for (int i = 0; i < 3; i++) spots.add(new ParkingSpot(SpotId.of("MD-" + i), SpotSize.MEDIUM, 0, idx++));
        for (int i = 0; i < 2; i++) spots.add(new ParkingSpot(SpotId.of("LG-" + i), SpotSize.LARGE, 0, idx++));
        ParkingLot lot = new ParkingLot("Downtown", List.of(new ParkingFloor(0, spots)));
        Map<SpotId, SpotSize> sizeOf = new HashMap<>();
        spots.forEach(s -> sizeOf.put(s.id(), s.size()));

        Clock.Manual clock = new Clock.Manual(0L);
        TicketRepository tickets = new InMemoryTicketRepository();
        ParkingLotService service = new ParkingLotService(
                lot, new BestFitStrategy(), new HourlyFeeStrategy(), tickets, clock);

        // ---- 1. 20 cars race to park into 5 car-compatible spots ----
        int cars = 20;
        ExecutorService pool = Executors.newFixedThreadPool(cars);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(cars);
        CopyOnWriteArrayList<Ticket> parked = new CopyOnWriteArrayList<>();
        AtomicInteger rejected = new AtomicInteger();

        for (int i = 0; i < cars; i++) {
            Vehicle car = new Vehicle(VehicleId.of("car-" + i), VehicleType.CAR);
            pool.submit(() -> {
                try {
                    start.await();
                    parked.add(service.park(car));
                } catch (ParkingFullException e) {
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

        long distinctSpots = parked.stream().map(Ticket::spotId).distinct().count();
        System.out.println("20 cars race for 5 spots -> parked=" + parked.size()
                + ", rejected=" + rejected.get() + " (expected 5 / 15)");
        System.out.println("distinct spots used = " + distinctSpots + " (must equal parked count -> no double allocation)");
        System.out.println("free spots remaining = " + service.freeSpots() + " (4 SMALL untouched by cars)");

        // ---- 2. A truck needs a LARGE spot, but cars filled both -> rejected ----
        Vehicle truck = new Vehicle(VehicleId.of("truck-1"), VehicleType.TRUCK);
        try {
            service.park(truck);
            System.out.println("ERROR: truck should not have parked");
        } catch (ParkingFullException e) {
            System.out.println("truck correctly rejected: " + e.getMessage());
        }

        // ---- 3. Unpark a car occupying a LARGE spot, advance 2.5h, show fee; truck then parks ----
        Ticket carOnLarge = parked.stream()
                .filter(t -> sizeOf.get(t.spotId()) == SpotSize.LARGE)
                .findFirst().orElseThrow();
        clock.advanceMillis(150 * 60_000L); // 2.5 hours
        Money fee = service.unpark(carOnLarge.id());
        System.out.println("car on " + carOnLarge.spotId().value() + " exits after 2.5h -> fee=" + fee
                + " (car @40/hr, 2.5h rounds up to 3h = 120.00)");
        parked.remove(carOnLarge);

        Ticket truckTicket = service.park(truck);
        System.out.println("truck now parks in freed LARGE spot -> " + truckTicket.spotId().value());

        // ---- 4. Freed spot is reusable: unpark a car on a MEDIUM spot, a new car takes it ----
        Ticket carOnMedium = parked.stream()
                .filter(t -> sizeOf.get(t.spotId()) == SpotSize.MEDIUM)
                .findFirst().orElseThrow();
        service.unpark(carOnMedium.id());
        Ticket newCar = service.park(new Vehicle(VehicleId.of("car-late"), VehicleType.CAR));
        System.out.println("freed " + carOnMedium.spotId().value() + "; new car reuses spot -> "
                + newCar.spotId().value() + " (size=" + sizeOf.get(newCar.spotId()) + ")");

        System.out.println("active tickets = " + tickets.activeCount() + ", free spots = " + service.freeSpots());
    }
}
