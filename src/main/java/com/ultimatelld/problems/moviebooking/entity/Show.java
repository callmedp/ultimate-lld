package com.ultimatelld.problems.moviebooking.entity;

import com.ultimatelld.problems.moviebooking.entity.Ids.HoldId;
import com.ultimatelld.problems.moviebooking.entity.Ids.SeatId;
import com.ultimatelld.problems.moviebooking.entity.Ids.ShowId;
import com.ultimatelld.problems.moviebooking.entity.Ids.UserId;
import com.ultimatelld.problems.moviebooking.exception.HoldExpiredException;
import com.ultimatelld.problems.moviebooking.exception.SeatUnavailableException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The aggregate root for seat state of a single show. ALL seat-status transitions go through this
 * object under its own {@link ReentrantLock}, which is what makes "exact-moment" seat locking safe:
 * the check-then-act sequence (verify AVAILABLE, then mark HELD) is atomic, so two concurrent users
 * can never both win the same seat.
 *
 * <p>Expired holds are reclaimed lazily on every entry point (and can also be swept proactively by a
 * background reaper calling {@link #releaseExpired}). Either way, an expired hold's seats return to
 * AVAILABLE before any new decision is made.
 */
public final class Show {

    private final ShowId id;
    private final String title;
    private final Map<SeatId, Seat> seats;
    private final Map<SeatId, SeatStatus> status = new HashMap<>();
    private final Map<HoldId, SeatHold> activeHolds = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public Show(ShowId id, String title, List<Seat> seatList) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        Objects.requireNonNull(seatList);
        if (seatList.isEmpty()) throw new IllegalArgumentException("show must have seats");
        Map<SeatId, Seat> map = new LinkedHashMap<>();
        for (Seat s : seatList) {
            map.put(s.id(), s);
            status.put(s.id(), SeatStatus.AVAILABLE);
        }
        this.seats = Map.copyOf(map);
    }

    public ShowId id() {
        return id;
    }

    public String title() {
        return title;
    }

    public Seat seat(SeatId seatId) {
        Seat s = seats.get(seatId);
        if (s == null) throw new IllegalArgumentException("no such seat: " + seatId.value());
        return s;
    }

    /**
     * Atomically holds all requested seats for a user, or fails without holding any (all-or-nothing).
     *
     * @throws SeatUnavailableException if any requested seat is not currently AVAILABLE.
     */
    public SeatHold hold(Set<SeatId> requested, UserId userId, HoldId holdId,
                         long nowMillis, long holdDurationMillis) {
        lock.lock();
        try {
            releaseExpiredInternal(nowMillis);

            for (SeatId seatId : requested) {
                SeatStatus st = status.get(seatId);
                if (st == null) throw new IllegalArgumentException("no such seat: " + seatId.value());
                if (st != SeatStatus.AVAILABLE) throw new SeatUnavailableException(seatId);
            }
            for (SeatId seatId : requested) {
                status.put(seatId, SeatStatus.HELD);
            }
            SeatHold heldHold = new SeatHold(holdId, id, userId, requested, nowMillis + holdDurationMillis);
            activeHolds.put(holdId, heldHold);
            return heldHold;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Atomically converts a still-valid hold into permanent BOOKED seats and consumes the hold.
     *
     * @throws HoldExpiredException if the hold is missing or expired (e.g. reclaimed by the reaper).
     */
    public SeatHold confirm(HoldId holdId, long nowMillis) {
        lock.lock();
        try {
            releaseExpiredInternal(nowMillis);
            SeatHold hold = activeHolds.get(holdId);
            if (hold == null || hold.isExpired(nowMillis)) {
                throw new HoldExpiredException(holdId);
            }
            for (SeatId seatId : hold.seats()) {
                status.put(seatId, SeatStatus.BOOKED);
            }
            activeHolds.remove(holdId);
            return hold;
        } finally {
            lock.unlock();
        }
    }

    /** Rolls booked seats back to AVAILABLE (used to compensate a failed payment). */
    public void releaseSeats(Set<SeatId> seatIds) {
        lock.lock();
        try {
            for (SeatId seatId : seatIds) {
                if (status.get(seatId) == SeatStatus.BOOKED) {
                    status.put(seatId, SeatStatus.AVAILABLE);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /** Proactively reclaim expired holds (a background reaper calls this periodically). */
    public int releaseExpired(long nowMillis) {
        lock.lock();
        try {
            return releaseExpiredInternal(nowMillis);
        } finally {
            lock.unlock();
        }
    }

    /** Must be called while holding {@link #lock}. */
    private int releaseExpiredInternal(long nowMillis) {
        List<HoldId> expired = new ArrayList<>();
        for (SeatHold hold : activeHolds.values()) {
            if (hold.isExpired(nowMillis)) expired.add(hold.id());
        }
        for (HoldId hid : expired) {
            SeatHold hold = activeHolds.remove(hid);
            for (SeatId seatId : hold.seats()) {
                if (status.get(seatId) == SeatStatus.HELD) {
                    status.put(seatId, SeatStatus.AVAILABLE);
                }
            }
        }
        return expired.size();
    }

    public SeatStatus statusOf(SeatId seatId) {
        lock.lock();
        try {
            return status.get(seatId);
        } finally {
            lock.unlock();
        }
    }

    public long countByStatus(SeatStatus target) {
        lock.lock();
        try {
            return status.values().stream().filter(s -> s == target).count();
        } finally {
            lock.unlock();
        }
    }
}
