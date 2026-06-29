package com.ultimatelld.problems.meetingscheduler.entity;

/**
 * A half-open time interval [start, end) measured in minutes-from-midnight (or any monotonic unit).
 * Half-open semantics mean [10,11) and [11,12) do NOT overlap — back-to-back bookings are allowed.
 */
public record Interval(int start, int end) {
    public Interval {
        if (start < 0 || end <= start) {
            throw new IllegalArgumentException("invalid interval [" + start + "," + end + ")");
        }
    }

    public boolean overlaps(Interval other) {
        return this.start < other.end && other.start < this.end;
    }

    @Override
    public String toString() {
        return "[" + start + "," + end + ")";
    }
}
