package com.ultimatelld.problems.meetingscheduler.driver;

import com.ultimatelld.problems.meetingscheduler.entity.Booking;
import com.ultimatelld.problems.meetingscheduler.entity.Interval;
import com.ultimatelld.problems.meetingscheduler.entity.MeetingRoom;
import com.ultimatelld.problems.meetingscheduler.exception.NoRoomAvailableException;
import com.ultimatelld.problems.meetingscheduler.service.MeetingSchedulerService;
import com.ultimatelld.problems.meetingscheduler.strategy.SmallestSufficientRoom;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Composition root + concurrent booking simulation for the meeting scheduler.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        List<MeetingRoom> rooms = List.of(
                new MeetingRoom("R-small", 4),
                new MeetingRoom("R-mid", 8),
                new MeetingRoom("R-large", 12));
        MeetingSchedulerService scheduler = new MeetingSchedulerService(rooms, new SmallestSufficientRoom());

        // ---- 1. Many threads book the SAME slot for 2 people -> only 3 succeed (one per room) ----
        Interval slot = new Interval(600, 660);   // 10:00-11:00
        int requests = 30;
        ExecutorService pool = Executors.newFixedThreadPool(requests);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(requests);
        CopyOnWriteArrayList<Booking> booked = new CopyOnWriteArrayList<>();
        AtomicInteger rejected = new AtomicInteger();

        for (int i = 0; i < requests; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    booked.add(scheduler.book(2, slot));
                } catch (NoRoomAvailableException e) {
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

        long distinctRooms = booked.stream().map(Booking::roomId).distinct().count();
        System.out.println("30 concurrent requests for slot " + slot + " -> booked=" + booked.size()
                + ", rejected=" + rejected.get() + " (expected 3 / 27, one per room)");
        System.out.println("distinct rooms booked = " + distinctRooms + " (must equal booked count -> no double-booking)");

        // ---- 2. Non-overlapping slots in the same room are fine; overlapping is rejected ----
        Booking b1 = scheduler.book(2, new Interval(660, 720));  // 11:00-12:00 reuses a room
        System.out.println("non-overlapping slot [660,720) booked in " + b1.roomId());
        Interval overlapping = new Interval(630, 690);            // overlaps the 10:00-11:00 slot in every room
        try {
            scheduler.book(2, overlapping);
            System.out.println("ERROR: overlapping booking should be rejected");
        } catch (NoRoomAvailableException e) {
            System.out.println("overlapping slot " + overlapping + " rejected (every room busy then): " + e.getMessage());
        }

        System.out.println("Per-room booking counts: "
                + rooms.stream().map(r -> r.id() + "=" + r.bookingCount()).toList());
    }
}
