package com.ultimatelld.problems.meetingscheduler.exception;

import com.ultimatelld.problems.meetingscheduler.entity.Interval;

/** Thrown when no room with sufficient capacity is free for the requested interval. */
public class NoRoomAvailableException extends RuntimeException {
    public NoRoomAvailableException(int attendees, Interval interval) {
        super("No room for " + attendees + " attendees at " + interval);
    }
}
