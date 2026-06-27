package com.ultimatelld.problems.parkinglot.entity;

import java.util.Objects;
import java.util.UUID;

/** Strongly-typed identifiers for the parking domain. */
public final class Ids {
    private Ids() {
    }

    public record SpotId(String value) {
        public SpotId {
            Objects.requireNonNull(value);
        }
        public static SpotId of(String v) {
            return new SpotId(v);
        }
    }

    public record VehicleId(String value) {
        public VehicleId {
            Objects.requireNonNull(value);
        }
        public static VehicleId of(String v) {
            return new VehicleId(v);
        }
    }

    public record TicketId(String value) {
        public TicketId {
            Objects.requireNonNull(value);
        }
        public static TicketId newId() {
            return new TicketId(UUID.randomUUID().toString());
        }
    }
}
