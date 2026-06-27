package com.ultimatelld.problems.moviebooking.repository;

import com.ultimatelld.problems.moviebooking.entity.Booking;
import com.ultimatelld.problems.moviebooking.entity.Ids.BookingId;

import java.util.Optional;

/** Persistence abstraction for bookings (DIP). */
public interface BookingRepository {
    void save(Booking booking);

    Optional<Booking> findById(BookingId id);

    int count();
}
