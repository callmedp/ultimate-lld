package com.ultimatelld.problems.elevator.driver;

import com.ultimatelld.problems.elevator.entity.Direction;
import com.ultimatelld.problems.elevator.entity.Elevator;
import com.ultimatelld.problems.elevator.entity.Request;
import com.ultimatelld.problems.elevator.service.ElevatorController;
import com.ultimatelld.problems.elevator.strategy.NearestCarStrategy;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Composition root: many concurrent hall calls, then a stepped simulation until every car is idle.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        int topFloor = 15;
        List<Elevator> cars = List.of(
                new Elevator(1, 0), new Elevator(2, 7), new Elevator(3, 15));
        ElevatorController controller = new ElevatorController(cars, new NearestCarStrategy());

        // ---- concurrent hall calls from many "passengers" ----
        int passengers = 40;
        Set<Integer> requestedFloors = ConcurrentHashMap.newKeySet();
        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(passengers);
        for (int i = 0; i < passengers; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    ThreadLocalRandom rnd = ThreadLocalRandom.current();
                    int floor = rnd.nextInt(1, topFloor + 1);
                    Direction dir = rnd.nextBoolean() ? Direction.UP : Direction.DOWN;
                    requestedFloors.add(floor);
                    controller.submitRequest(new Request(floor, dir));
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
        System.out.println("Dispatched " + passengers + " concurrent hall calls to "
                + requestedFloors.size() + " distinct floors across 3 cars.");

        // ---- run the simulation to completion ----
        int ticks = 0;
        int totalServed = 0;
        while (!controller.allIdle() && ticks < 1000) {
            totalServed += controller.step();
            ticks++;
        }

        Set<Integer> servicedFloors = ConcurrentHashMap.newKeySet();
        for (Elevator e : cars) {
            servicedFloors.addAll(e.servicedFloors());
            System.out.println("  car-" + e.id() + " finished at floor " + e.currentFloor()
                    + " (" + e.direction() + "), stops served=" + e.servicedFloors().size());
        }
        boolean allServed = servicedFloors.containsAll(requestedFloors);
        System.out.println("Simulation: ticks=" + ticks + ", totalStopsServed=" + totalServed
                + ", allIdle=" + controller.allIdle());
        System.out.println("Every requested floor was visited? " + allServed
                + " (requested=" + requestedFloors.size() + ", serviced=" + servicedFloors.size() + ")");
    }
}
