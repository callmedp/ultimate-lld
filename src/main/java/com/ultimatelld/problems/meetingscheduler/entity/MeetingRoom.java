package com.ultimatelld.problems.meetingscheduler.entity;

import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A bookable room. Its booking calendar (a {@link TreeMap} keyed by interval start) is guarded by a
 * per-room {@link ReentrantLock}, so the conflict check and the insert are one atomic step — two
 * threads can never both book the same room for overlapping intervals.
 */
public final class MeetingRoom {

    private final String id;
    private final int capacity;
    private final TreeMap<Integer, Interval> bookings = new TreeMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public MeetingRoom(String id, int capacity) {
        this.id = Objects.requireNonNull(id);
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        this.capacity = capacity;
    }

    public String id() {
        return id;
    }

    public int capacity() {
        return capacity;
    }

    /** Atomically books the interval if it doesn't conflict; returns false on conflict. */
    public boolean tryBook(Interval iv) {
        lock.lock();
        try {
            // Check the nearest bookings on either side of the new start — O(log n).
            var floor = bookings.floorEntry(iv.start());
            if (floor != null && floor.getValue().overlaps(iv)) return false;
            var ceiling = bookings.ceilingEntry(iv.start());
            if (ceiling != null && ceiling.getValue().overlaps(iv)) return false;
            bookings.put(iv.start(), iv);
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void cancel(Interval iv) {
        lock.lock();
        try {
            bookings.remove(iv.start(), iv);
        } finally {
            lock.unlock();
        }
    }

    public int bookingCount() {
        lock.lock();
        try {
            return bookings.size();
        } finally {
            lock.unlock();
        }
    }

    public List<Interval> bookings() {
        lock.lock();
        try {
            return List.copyOf(bookings.values());
        } finally {
            lock.unlock();
        }
    }
}
