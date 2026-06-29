package com.ultimatelld.problems.meetingscheduler.strategy;

import com.ultimatelld.problems.meetingscheduler.entity.Interval;
import com.ultimatelld.problems.meetingscheduler.entity.MeetingRoom;

import java.util.Comparator;
import java.util.List;

/** Prefer the smallest room that still fits — preserves big rooms for big meetings. */
public final class SmallestSufficientRoom implements RoomSelectionStrategy {
    @Override
    public List<MeetingRoom> orderCandidates(List<MeetingRoom> sufficientRooms, int attendees, Interval interval) {
        return sufficientRooms.stream()
                .sorted(Comparator.comparingInt(MeetingRoom::capacity).thenComparing(MeetingRoom::id))
                .toList();
    }

    @Override
    public String name() {
        return "SMALLEST_SUFFICIENT";
    }
}
