package com.ultimatelld.problems.urlshortener.service;

import com.ultimatelld.problems.urlshortener.core.IdGenerator;
import com.ultimatelld.problems.urlshortener.exception.AliasAlreadyExistsException;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Orchestrates shortening/expansion. Dependencies (id generation, persistence) are injected
 * abstractions, so the algorithm and storage can change independently (DIP).
 */
public final class UrlShortenerService {

    private final UrlRepository repository;
    private final IdGenerator idGenerator;

    public UrlShortenerService(UrlRepository repository, IdGenerator idGenerator) {
        this.repository = Objects.requireNonNull(repository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    /** Idempotent: the same URL always yields the same short code. */
    public String shorten(String longUrl) {
        validateUrl(longUrl);
        return repository.getOrCreateCode(longUrl, idGenerator::next);
    }

    public String shortenWithAlias(String longUrl, String alias) {
        validateUrl(longUrl);
        if (alias == null || alias.isBlank()) throw new IllegalArgumentException("alias must not be blank");
        if (!repository.reserveAlias(alias, longUrl)) {
            throw new AliasAlreadyExistsException(alias);
        }
        return alias;
    }

    public String expand(String code) {
        return repository.findUrl(code)
                .orElseThrow(() -> new NoSuchElementException("unknown short code: " + code));
    }

    private void validateUrl(String longUrl) {
        if (longUrl == null || longUrl.isBlank()) throw new IllegalArgumentException("url must not be blank");
    }
}
