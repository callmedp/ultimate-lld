package com.ultimatelld.problems.cabbooking.entity;

/** A 2-D point; distance is plain Euclidean (a real system would use road/ETA distance). */
public record Location(double x, double y) {
    public double distanceTo(Location other) {
        double dx = x - other.x;
        double dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
