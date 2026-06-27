package com.ultimatelld.problems.moviebooking.driver;

import com.ultimatelld.problems.moviebooking.entity.Booking;
import com.ultimatelld.problems.moviebooking.entity.Ids.HoldId;
import com.ultimatelld.problems.moviebooking.entity.Ids.SeatId;
import com.ultimatelld.problems.moviebooking.entity.Ids.ShowId;
import com.ultimatelld.problems.moviebooking.entity.Ids.UserId;
import com.ultimatelld.problems.moviebooking.entity.Seat;
import com.ultimatelld.problems.moviebooking.entity.SeatStatus;
import com.ultimatelld.problems.moviebooking.entity.SeatTier;
import com.ultimatelld.problems.moviebooking.entity.Show;
import com.ultimatelld.problems.moviebooking.exception.HoldExpiredException;
import com.ultimatelld.problems.moviebooking.exception.PaymentDeclinedException;
import com.ultimatelld.problems.moviebooking.exception.SeatUnavailableException;
import com.ultimatelld.problems.moviebooking.repository.BookingRepository;
import com.ultimatelld.problems.moviebooking.repository.InMemoryBookingRepository;
import com.ultimatelld.problems.moviebooking.repository.InMemoryShowRepository;
import com.ultimatelld.problems.moviebooking.repository.ShowRepository;
import com.ultimatelld.problems.moviebooking.service.AlwaysApprovePaymentGateway;
import com.ultimatelld.problems.moviebooking.service.BookingService;
import com.ultimatelld.problems.moviebooking.service.PaymentGateway;
import com.ultimatelld.problems.moviebooking.strategy.StandardPricing;
import com.ultimatelld.problems.moviebooking.util.ManualClock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Composition root + concurrent booking simulation.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        ManualClock clock = new ManualClock(0L);
        long holdMillis = 1_000;

        ShowRepository showRepo = new InMemoryShowRepository();
        BookingRepository bookingRepo = new InMemoryBookingRepository();
        PaymentGateway payments = new AlwaysApprovePaymentGateway(Set.of("bad-user"));
        BookingService service = new BookingService(showRepo, bookingRepo, payments,
                new StandardPricing(), clock, holdMillis);

        // Build a show with 20 seats (S1..S20), alternating tiers.
        ShowId showId = ShowId.of("show-1");
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            SeatTier tier = (i % 5 == 0) ? SeatTier.PLATINUM : (i % 2 == 0 ? SeatTier.GOLD : SeatTier.SILVER);
            seats.add(new Seat(SeatId.of("S" + i), tier));
        }
        showRepo.save(new Show(showId, "Interstellar", seats));

        // ---- 1. 50 threads race to lock the SAME seat S1: exactly one must win ----
        int threads = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger won = new AtomicInteger();
        AtomicInteger lost = new AtomicInteger();
        AtomicReference<HoldId> winningHold = new AtomicReference<>();

        for (int i = 0; i < threads; i++) {
            UserId user = UserId.of("user-" + i);
            pool.submit(() -> {
                try {
                    start.await();
                    HoldId h = service.lockSeats(showId, Set.of(SeatId.of("S1")), user);
                    won.incrementAndGet();
                    winningHold.set(h);
                } catch (SeatUnavailableException e) {
                    lost.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        pool.shutdownNow();
        System.out.println("50 threads race to lock seat S1 -> won=" + won.get()
                + ", lost=" + lost.get() + " (expected won=1, lost=49)");

        // ---- 2. The winner confirms -> booking created, S1 becomes BOOKED ----
        Booking booking = service.confirmBooking(winningHold.get());
        System.out.println("Winner confirmed -> " + booking);
        System.out.println("S1 status now = " + showRepo.findById(showId).orElseThrow().statusOf(SeatId.of("S1")));

        // ---- 3. Hold expiry: lock S2, let it expire, another user re-locks it ----
        HoldId expiringHold = service.lockSeats(showId, Set.of(SeatId.of("S2")), UserId.of("alice"));
        System.out.println("alice holds S2; status=" + showRepo.findById(showId).orElseThrow().statusOf(SeatId.of("S2")));
        clock.advanceMillis(holdMillis + 1);   // expire the hold
        HoldId bobHold = service.lockSeats(showId, Set.of(SeatId.of("S2")), UserId.of("bob"));
        System.out.println("after expiry, bob re-locks S2 successfully (lazy reclaim).");
        try {
            service.confirmBooking(expiringHold); // alice's hold is gone
            System.out.println("ERROR: alice's expired hold should not confirm");
        } catch (HoldExpiredException e) {
            System.out.println("alice's expired hold correctly rejected: " + e.getMessage());
        }
        Booking bobBooking = service.confirmBooking(bobHold);
        System.out.println("bob confirmed S2 -> " + bobBooking);

        // ---- 4. Payment failure rolls the seat back to AVAILABLE ----
        HoldId badHold = service.lockSeats(showId, Set.of(SeatId.of("S3")), UserId.of("bad-user"));
        try {
            service.confirmBooking(badHold);
            System.out.println("ERROR: declined payment should have thrown");
        } catch (PaymentDeclinedException e) {
            System.out.println("payment declined -> " + e.getMessage());
        }
        SeatStatus s3 = showRepo.findById(showId).orElseThrow().statusOf(SeatId.of("S3"));
        System.out.println("S3 rolled back to " + s3 + " after failed payment (expected AVAILABLE)");

        Show show = showRepo.findById(showId).orElseThrow();
        System.out.println("Final tally -> bookings=" + bookingRepo.count()
                + ", BOOKED seats=" + show.countByStatus(SeatStatus.BOOKED)
                + ", AVAILABLE=" + show.countByStatus(SeatStatus.AVAILABLE));
    }
}
