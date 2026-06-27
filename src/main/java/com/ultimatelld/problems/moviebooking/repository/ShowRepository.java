package com.ultimatelld.problems.moviebooking.repository;

import com.ultimatelld.problems.moviebooking.entity.Ids.ShowId;
import com.ultimatelld.problems.moviebooking.entity.Show;

import java.util.Collection;
import java.util.Optional;

/** Persistence abstraction for shows (DIP). */
public interface ShowRepository {
    void save(Show show);

    Optional<Show> findById(ShowId id);

    Collection<Show> findAll();
}
