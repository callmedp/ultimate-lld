package com.ultimatelld.problems.moviebooking.entity;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifiers for the booking domain, grouped in one file for brevity.
 * Each is a distinct type so a SeatId can never be passed where a ShowId is expected.
 */
public final class Ids {
    private Ids() {
    }

    public record ShowId(String value) {
        public ShowId {
            Objects.requireNonNull(value);
        }
        public static ShowId of(String v) {
            return new ShowId(v);
        }
    }

    public record SeatId(String value) {
        public SeatId {
            Objects.requireNonNull(value);
        }
        public static SeatId of(String v) {
            return new SeatId(v);
        }
    }

    public record UserId(String value) {
        public UserId {
            Objects.requireNonNull(value);
        }
        public static UserId of(String v) {
            return new UserId(v);
        }
    }

    public record HoldId(String value) {
        public HoldId {
            Objects.requireNonNull(value);
        }
        public static HoldId newId() {
            return new HoldId(UUID.randomUUID().toString());
        }
    }

    public record BookingId(String value) {
        public BookingId {
            Objects.requireNonNull(value);
        }
        public static BookingId newId() {
            return new BookingId(UUID.randomUUID().toString());
        }
    }
}
