package com.ultimatelld.problems.meetingscheduler.entity;

import java.util.Objects;
import java.util.UUID;

/** Confirmed reservation of a room for an interval. */
public record Booking(String id, String roomId, Interval interval, int attendees) {
    public Booking {
        Objects.requireNonNull(roomId);
        Objects.requireNonNull(interval);
    }

    public static Booking create(String roomId, Interval interval, int attendees) {
        return new Booking(UUID.randomUUID().toString(), roomId, interval, attendees);
    }
}
