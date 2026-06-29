package com.ultimatelld.problems.meetingscheduler.strategy;

import com.ultimatelld.problems.meetingscheduler.entity.Interval;
import com.ultimatelld.problems.meetingscheduler.entity.MeetingRoom;

import java.util.List;

/**
 * OCP policy that ORDERS candidate rooms (already filtered to sufficient capacity) for the service
 * to attempt booking. A pure ordering function — the atomic booking happens in the room/service.
 */
public interface RoomSelectionStrategy {
    List<MeetingRoom> orderCandidates(List<MeetingRoom> sufficientRooms, int attendees, Interval interval);

    String name();
}
