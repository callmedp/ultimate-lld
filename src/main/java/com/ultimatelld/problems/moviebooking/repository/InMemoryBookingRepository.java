package com.ultimatelld.problems.moviebooking.repository;

import com.ultimatelld.problems.moviebooking.entity.Booking;
import com.ultimatelld.problems.moviebooking.entity.Ids.BookingId;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe in-memory booking store. */
public final class InMemoryBookingRepository implements BookingRepository {

    private final ConcurrentHashMap<BookingId, Booking> store = new ConcurrentHashMap<>();

    @Override
    public void save(Booking booking) {
        store.put(booking.id(), booking);
    }

    @Override
    public Optional<Booking> findById(BookingId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public int count() {
        return store.size();
    }
}
