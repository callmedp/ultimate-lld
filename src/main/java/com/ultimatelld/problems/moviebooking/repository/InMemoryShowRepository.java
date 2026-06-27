package com.ultimatelld.problems.moviebooking.repository;

import com.ultimatelld.problems.moviebooking.entity.Ids.ShowId;
import com.ultimatelld.problems.moviebooking.entity.Show;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe in-memory show store. The Show object itself owns its internal concurrency. */
public final class InMemoryShowRepository implements ShowRepository {

    private final ConcurrentHashMap<ShowId, Show> store = new ConcurrentHashMap<>();

    @Override
    public void save(Show show) {
        store.put(show.id(), show);
    }

    @Override
    public Optional<Show> findById(ShowId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Collection<Show> findAll() {
        return List.copyOf(store.values());
    }
}
