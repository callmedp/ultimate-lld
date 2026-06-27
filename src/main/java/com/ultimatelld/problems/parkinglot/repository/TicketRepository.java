package com.ultimatelld.problems.parkinglot.repository;

import com.ultimatelld.problems.parkinglot.entity.Ids.TicketId;
import com.ultimatelld.problems.parkinglot.entity.Ticket;

import java.util.Optional;

/** Persistence abstraction for active tickets (DIP). */
public interface TicketRepository {
    void save(Ticket ticket);

    Optional<Ticket> findById(TicketId id);

    void remove(TicketId id);

    int activeCount();
}
