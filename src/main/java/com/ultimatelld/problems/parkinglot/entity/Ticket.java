package com.ultimatelld.problems.parkinglot.entity;

import com.ultimatelld.problems.parkinglot.entity.Ids.SpotId;
import com.ultimatelld.problems.parkinglot.entity.Ids.TicketId;
import com.ultimatelld.problems.parkinglot.entity.Ids.VehicleId;

import java.util.Objects;

/** Issued on entry; carries everything needed to compute the fee on exit. */
public record Ticket(TicketId id, SpotId spotId, VehicleId vehicleId,
                     VehicleType vehicleType, long entryMillis) {
    public Ticket {
        Objects.requireNonNull(id);
        Objects.requireNonNull(spotId);
        Objects.requireNonNull(vehicleId);
        Objects.requireNonNull(vehicleType);
    }
}
