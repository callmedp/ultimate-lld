package com.ultimatelld.problems.urlshortener.service;

import java.util.Optional;
import java.util.function.Supplier;

/** Persistence abstraction for code<->URL mappings (DIP). */
public interface UrlRepository {

    /**
     * Returns the existing code for {@code longUrl}, or atomically creates one via {@code codeFactory}
     * if absent. Guarantees the same URL always maps to the same code (idempotent shortening).
     */
    String getOrCreateCode(String longUrl, Supplier<String> codeFactory);

    /** Atomically reserve a custom alias for a URL; false if the alias is already taken. */
    boolean reserveAlias(String alias, String longUrl);

    Optional<String> findUrl(String code);

    int size();
}
