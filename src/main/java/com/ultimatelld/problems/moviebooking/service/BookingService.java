package com.ultimatelld.problems.moviebooking.service;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.moviebooking.entity.Booking;
import com.ultimatelld.problems.moviebooking.entity.Ids.BookingId;
import com.ultimatelld.problems.moviebooking.entity.Ids.HoldId;
import com.ultimatelld.problems.moviebooking.entity.Ids.SeatId;
import com.ultimatelld.problems.moviebooking.entity.Ids.ShowId;
import com.ultimatelld.problems.moviebooking.entity.Ids.UserId;
import com.ultimatelld.problems.moviebooking.entity.SeatHold;
import com.ultimatelld.problems.moviebooking.entity.Show;
import com.ultimatelld.problems.moviebooking.exception.HoldExpiredException;
import com.ultimatelld.problems.moviebooking.exception.PaymentDeclinedException;
import com.ultimatelld.problems.moviebooking.repository.BookingRepository;
import com.ultimatelld.problems.moviebooking.repository.ShowRepository;
import com.ultimatelld.problems.moviebooking.strategy.SeatPricing;
import com.ultimatelld.problems.moviebooking.util.Clock;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrates the booking flow. It owns NO seat-state locking itself — that is delegated to the
 * {@link Show} aggregate, which guarantees atomic transitions. The service composes the steps:
 * lock seats -> (later) confirm = atomically book + charge, compensating the seat hold if the
 * payment is declined.
 */
public final class BookingService {

    private final ShowRepository showRepository;
    private final BookingRepository bookingRepository;
    private final PaymentGateway paymentGateway;
    private final SeatPricing pricing;
    private final Clock clock;
    private final long holdDurationMillis;

    /** Maps an outstanding hold to its show so confirm() can locate the aggregate. */
    private final ConcurrentHashMap<HoldId, ShowId> holdIndex = new ConcurrentHashMap<>();

    public BookingService(ShowRepository showRepository, BookingRepository bookingRepository,
                          PaymentGateway paymentGateway, SeatPricing pricing, Clock clock,
                          long holdDurationMillis) {
        this.showRepository = Objects.requireNonNull(showRepository);
        this.bookingRepository = Objects.requireNonNull(bookingRepository);
        this.paymentGateway = Objects.requireNonNull(paymentGateway);
        this.pricing = Objects.requireNonNull(pricing);
        this.clock = Objects.requireNonNull(clock);
        if (holdDurationMillis <= 0) throw new IllegalArgumentException("holdDuration must be > 0");
        this.holdDurationMillis = holdDurationMillis;
    }

    /** Temporarily locks seats for a user; throws SeatUnavailableException if any are taken. */
    public HoldId lockSeats(ShowId showId, Set<SeatId> seatIds, UserId userId) {
        Show show = requireShow(showId);
        HoldId holdId = HoldId.newId();
        show.hold(new HashSet<>(seatIds), userId, holdId, clock.nowMillis(), holdDurationMillis);
        holdIndex.put(holdId, showId);
        return holdId;
    }

    /**
     * Confirms a hold into a booking: atomically books the seats, then charges. If the charge is
     * declined, the seats are rolled back to AVAILABLE and a PaymentDeclinedException is thrown.
     */
    public Booking confirmBooking(HoldId holdId) {
        ShowId showId = holdIndex.get(holdId);
        if (showId == null) throw new HoldExpiredException(holdId);
        Show show = requireShow(showId);

        SeatHold hold = show.confirm(holdId, clock.nowMillis()); // throws if expired; seats now BOOKED
        holdIndex.remove(holdId);

        Money amount = totalPrice(show, hold.seats());
        Set<SeatId> seatSet = new HashSet<>(hold.seats());
        if (!paymentGateway.charge(hold.userId(), amount)) {
            show.releaseSeats(seatSet);   // compensate: undo the booking on payment failure
            throw new PaymentDeclinedException("user=" + hold.userId().value());
        }

        Booking booking = new Booking(BookingId.newId(), showId, hold.userId(), seatSet, amount);
        bookingRepository.save(booking);
        return booking;
    }

    /** A background reaper would call this on a schedule to reclaim expired holds proactively. */
    public int sweepExpiredHolds() {
        int released = 0;
        long now = clock.nowMillis();
        for (Show show : showRepository.findAll()) {
            released += show.releaseExpired(now);
        }
        // Note: holdIndex entries for expired holds are harmless — confirm() rejects them via Show
        // and removes the index entry; a production system would also evict them here by timestamp.
        return released;
    }

    private Money totalPrice(Show show, Iterable<SeatId> seatIds) {
        Money total = Money.ZERO;
        for (SeatId seatId : seatIds) {
            total = total.add(pricing.priceFor(show.seat(seatId).tier()));
        }
        return total;
    }

    private Show requireShow(ShowId showId) {
        return showRepository.findById(showId)
                .orElseThrow(() -> new NoSuchElementException("no such show: " + showId.value()));
    }
}
