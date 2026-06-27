package com.ultimatelld.problems.moviebooking.entity;

import com.ultimatelld.problems.moviebooking.entity.Ids.HoldId;
import com.ultimatelld.problems.moviebooking.entity.Ids.SeatId;
import com.ultimatelld.problems.moviebooking.entity.Ids.ShowId;
import com.ultimatelld.problems.moviebooking.entity.Ids.UserId;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/** A temporary reservation of seats for a user, valid until {@code expiresAtMillis}. */
public final class SeatHold {

    private final HoldId id;
    private final ShowId showId;
    private final UserId userId;
    private final Set<SeatId> seats;
    private final long expiresAtMillis;

    public SeatHold(HoldId id, ShowId showId, UserId userId, Set<SeatId> seats, long expiresAtMillis) {
        this.id = Objects.requireNonNull(id);
        this.showId = Objects.requireNonNull(showId);
        this.userId = Objects.requireNonNull(userId);
        this.seats = Set.copyOf(seats);
        this.expiresAtMillis = expiresAtMillis;
    }

    public HoldId id() {
        return id;
    }

    public ShowId showId() {
        return showId;
    }

    public UserId userId() {
        return userId;
    }

    public List<SeatId> seats() {
        return List.copyOf(seats);
    }

    public boolean isExpired(long nowMillis) {
        return nowMillis >= expiresAtMillis;
    }
}
