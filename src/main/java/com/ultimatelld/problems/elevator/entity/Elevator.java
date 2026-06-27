package com.ultimatelld.problems.elevator.entity;

import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A single elevator car. Thread-safe: all mutable state (current floor, direction, pending stops)
 * is guarded by a per-car {@link ReentrantLock}, so stops can be added from many request threads
 * while a controller steps the car. Movement uses the LOOK algorithm — keep going in the current
 * direction servicing stops, then reverse when none remain ahead.
 */
public final class Elevator {

    private final int id;
    private final ReentrantLock lock = new ReentrantLock();
    private final NavigableSet<Integer> stops = new TreeSet<>();
    private final CopyOnWriteArrayList<Integer> serviced = new CopyOnWriteArrayList<>();

    private int currentFloor;
    private Direction direction = Direction.IDLE;

    public Elevator(int id, int startFloor) {
        this.id = id;
        this.currentFloor = startFloor;
    }

    public int id() {
        return id;
    }

    public void addStop(int floor) {
        lock.lock();
        try {
            if (floor == currentFloor) {
                serviced.add(floor);   // already here; serve immediately
                return;
            }
            stops.add(floor);
            if (direction == Direction.IDLE) {
                direction = floor > currentFloor ? Direction.UP : Direction.DOWN;
            }
        } finally {
            lock.unlock();
        }
    }

    /** Advances the car by at most one floor toward its next stop. Returns true if a stop was served. */
    public boolean step() {
        lock.lock();
        try {
            if (stops.isEmpty()) {
                direction = Direction.IDLE;
                return false;
            }
            Integer target = nextTarget();
            if (target == null) {
                direction = Direction.IDLE;
                return false;
            }
            currentFloor += (target > currentFloor) ? 1 : -1;
            boolean servedHere = stops.remove(currentFloor);
            if (servedHere) {
                serviced.add(currentFloor);
            }
            if (stops.isEmpty()) {
                direction = Direction.IDLE;   // nothing left to do; reflect idleness
            }
            return servedHere;
        } finally {
            lock.unlock();
        }
    }

    /** LOOK: prefer a stop ahead in the current direction; otherwise reverse. Caller holds the lock. */
    private Integer nextTarget() {
        if (direction == Direction.UP) {
            Integer up = stops.ceiling(currentFloor);
            if (up != null) return up;
            direction = Direction.DOWN;
            return stops.floor(currentFloor);
        }
        if (direction == Direction.DOWN) {
            Integer down = stops.floor(currentFloor);
            if (down != null) return down;
            direction = Direction.UP;
            return stops.ceiling(currentFloor);
        }
        // IDLE: head toward the nearest pending stop
        Integer up = stops.ceiling(currentFloor);
        Integer down = stops.floor(currentFloor);
        if (up == null) return down;
        if (down == null) return up;
        int chosen = (up - currentFloor) <= (currentFloor - down) ? up : down;
        direction = chosen >= currentFloor ? Direction.UP : Direction.DOWN;
        return chosen;
    }

    public int currentFloor() {
        lock.lock();
        try {
            return currentFloor;
        } finally {
            lock.unlock();
        }
    }

    public Direction direction() {
        lock.lock();
        try {
            return direction;
        } finally {
            lock.unlock();
        }
    }

    public boolean isIdle() {
        lock.lock();
        try {
            return stops.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public int pendingStops() {
        lock.lock();
        try {
            return stops.size();
        } finally {
            lock.unlock();
        }
    }

    public java.util.List<Integer> servicedFloors() {
        return java.util.List.copyOf(serviced);
    }
}
