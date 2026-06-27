package com.ultimatelld.problems.moviebooking.entity;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.moviebooking.entity.Ids.BookingId;
import com.ultimatelld.problems.moviebooking.entity.Ids.SeatId;
import com.ultimatelld.problems.moviebooking.entity.Ids.ShowId;
import com.ultimatelld.problems.moviebooking.entity.Ids.UserId;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/** A confirmed booking: seats permanently allocated to a user, with the amount charged. */
public final class Booking {

    private final BookingId id;
    private final ShowId showId;
    private final UserId userId;
    private final Set<SeatId> seats;
    private final Money amount;
    private final BookingStatus status;

    public Booking(BookingId id, ShowId showId, UserId userId, Set<SeatId> seats, Money amount) {
        this.id = Objects.requireNonNull(id);
        this.showId = Objects.requireNonNull(showId);
        this.userId = Objects.requireNonNull(userId);
        this.seats = Set.copyOf(seats);
        this.amount = Objects.requireNonNull(amount);
        this.status = BookingStatus.CONFIRMED;
    }

    public BookingId id() {
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

    public Money amount() {
        return amount;
    }

    public BookingStatus status() {
        return status;
    }

    @Override
    public String toString() {
        return "Booking{" + id.value().substring(0, 8) + ", user=" + userId.value()
                + ", seats=" + seats.size() + ", amount=" + amount + ", " + status + "}";
    }
}
