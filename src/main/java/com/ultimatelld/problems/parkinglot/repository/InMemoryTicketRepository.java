package com.ultimatelld.problems.parkinglot.repository;

import com.ultimatelld.problems.parkinglot.entity.Ids.TicketId;
import com.ultimatelld.problems.parkinglot.entity.Ticket;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe in-memory ticket store. */
public final class InMemoryTicketRepository implements TicketRepository {

    private final ConcurrentHashMap<TicketId, Ticket> store = new ConcurrentHashMap<>();

    @Override
    public void save(Ticket ticket) {
        store.put(ticket.id(), ticket);
    }

    @Override
    public Optional<Ticket> findById(TicketId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void remove(TicketId id) {
        store.remove(id);
    }

    @Override
    public int activeCount() {
        return store.size();
    }
}
