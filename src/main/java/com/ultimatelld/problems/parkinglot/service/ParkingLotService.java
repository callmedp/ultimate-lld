package com.ultimatelld.problems.parkinglot.service;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.parkinglot.entity.Ids.TicketId;
import com.ultimatelld.problems.parkinglot.entity.ParkingLot;
import com.ultimatelld.problems.parkinglot.entity.ParkingSpot;
import com.ultimatelld.problems.parkinglot.entity.Ticket;
import com.ultimatelld.problems.parkinglot.entity.Vehicle;
import com.ultimatelld.problems.parkinglot.exception.ParkingFullException;
import com.ultimatelld.problems.parkinglot.repository.TicketRepository;
import com.ultimatelld.problems.parkinglot.strategy.FeeStrategy;
import com.ultimatelld.problems.parkinglot.strategy.SpotAllocationStrategy;
import com.ultimatelld.problems.parkinglot.util.Clock;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrates entry/exit. Concurrency is handled WITHOUT a global lock: the allocation strategy
 * proposes an ordered list of candidate spots and the service walks it, attempting an atomic
 * {@link ParkingSpot#tryOccupy} (CAS) on each. The first successful CAS wins the spot; if a
 * concurrent vehicle took it, the CAS fails and we simply try the next candidate. This makes
 * "two cars race for the last spot" safe and lock-free — at most one can win any given spot.
 */
public final class ParkingLotService {

    private final ParkingLot lot;
    private final SpotAllocationStrategy allocationStrategy;
    private final FeeStrategy feeStrategy;
    private final TicketRepository ticketRepository;
    private final Clock clock;

    /** Remembers which spot a ticket occupies, so exit can release exactly that spot. */
    private final ConcurrentHashMap<TicketId, ParkingSpot> spotByTicket = new ConcurrentHashMap<>();

    public ParkingLotService(ParkingLot lot, SpotAllocationStrategy allocationStrategy,
                             FeeStrategy feeStrategy, TicketRepository ticketRepository, Clock clock) {
        this.lot = Objects.requireNonNull(lot);
        this.allocationStrategy = Objects.requireNonNull(allocationStrategy);
        this.feeStrategy = Objects.requireNonNull(feeStrategy);
        this.ticketRepository = Objects.requireNonNull(ticketRepository);
        this.clock = Objects.requireNonNull(clock);
    }

    /** Parks a vehicle, returning its ticket. Throws ParkingFullException if no spot is claimable. */
    public Ticket park(Vehicle vehicle) {
        List<ParkingSpot> fitting = lot.allSpots().stream()
                .filter(s -> s.canFit(vehicle.type()))
                .toList();
        List<ParkingSpot> ordered = allocationStrategy.orderCandidates(fitting, vehicle);

        for (ParkingSpot spot : ordered) {
            if (spot.isFree() && spot.tryOccupy(vehicle.id())) {     // atomic claim
                Ticket ticket = new Ticket(TicketId.newId(), spot.id(), vehicle.id(),
                        vehicle.type(), clock.nowMillis());
                ticketRepository.save(ticket);
                spotByTicket.put(ticket.id(), spot);
                return ticket;
            }
        }
        throw new ParkingFullException(vehicle.type());
    }

    /** Releases the spot for a ticket and returns the computed fee. */
    public Money unpark(TicketId ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("no such ticket: " + ticketId.value()));
        ParkingSpot spot = spotByTicket.remove(ticketId);
        if (spot != null) {
            spot.vacate(ticket.vehicleId());
        }
        ticketRepository.remove(ticketId);
        long duration = clock.nowMillis() - ticket.entryMillis();
        return feeStrategy.fee(ticket.vehicleType(), duration);
    }

    public long freeSpots() {
        return lot.allSpots().stream().filter(ParkingSpot::isFree).count();
    }
}
