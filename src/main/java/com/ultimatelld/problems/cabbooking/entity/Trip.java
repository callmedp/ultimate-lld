package com.ultimatelld.problems.cabbooking.entity;

import com.ultimatelld.common.Money;

import java.util.Objects;
import java.util.UUID;

/** A matched trip: which driver serves which rider from a pickup, and the quoted fare. */
public record Trip(String id, String driverId, String riderId, Location pickup, Money fare) {
    public Trip {
        Objects.requireNonNull(driverId);
        Objects.requireNonNull(riderId);
        Objects.requireNonNull(pickup);
        Objects.requireNonNull(fare);
    }

    public static Trip create(String driverId, String riderId, Location pickup, Money fare) {
        return new Trip(UUID.randomUUID().toString(), driverId, riderId, pickup, fare);
    }
}
