package com.ultimatelld.problems.meetingscheduler.service;

import com.ultimatelld.problems.meetingscheduler.entity.Booking;
import com.ultimatelld.problems.meetingscheduler.entity.Interval;
import com.ultimatelld.problems.meetingscheduler.entity.MeetingRoom;
import com.ultimatelld.problems.meetingscheduler.exception.NoRoomAvailableException;
import com.ultimatelld.problems.meetingscheduler.strategy.RoomSelectionStrategy;

import java.util.List;
import java.util.Objects;

/**
 * Orchestrates booking: filter rooms by capacity, order them via the injected
 * {@link RoomSelectionStrategy}, then attempt an ATOMIC {@link MeetingRoom#tryBook} on each in turn.
 * The first room that accepts wins; a room that lost a concurrent race simply rejects and we move on.
 * No global lock — rooms are booked independently.
 */
public final class MeetingSchedulerService {

    private final List<MeetingRoom> rooms;
    private final RoomSelectionStrategy strategy;

    public MeetingSchedulerService(List<MeetingRoom> rooms, RoomSelectionStrategy strategy) {
        if (rooms.isEmpty()) throw new IllegalArgumentException("need at least one room");
        this.rooms = List.copyOf(rooms);
        this.strategy = Objects.requireNonNull(strategy);
    }

    public Booking book(int attendees, Interval interval) {
        if (attendees <= 0) throw new IllegalArgumentException("attendees must be > 0");
        List<MeetingRoom> sufficient = rooms.stream()
                .filter(r -> r.capacity() >= attendees)
                .toList();
        for (MeetingRoom room : strategy.orderCandidates(sufficient, attendees, interval)) {
            if (room.tryBook(interval)) {
                return Booking.create(room.id(), interval, attendees);
            }
        }
        throw new NoRoomAvailableException(attendees, interval);
    }

    public List<MeetingRoom> rooms() {
        return rooms;
    }
}
