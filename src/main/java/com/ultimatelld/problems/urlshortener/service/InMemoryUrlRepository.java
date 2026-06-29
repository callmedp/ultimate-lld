package com.ultimatelld.problems.urlshortener.service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Thread-safe in-memory repository. Idempotent shortening relies on
 * {@link ConcurrentHashMap#computeIfAbsent} being atomic per key: concurrent calls to shorten the
 * same URL all observe a single code generation. Custom aliases use {@code putIfAbsent} for an
 * atomic claim.
 */
public final class InMemoryUrlRepository implements UrlRepository {

    private final ConcurrentHashMap<String, String> codeToUrl = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> urlToCode = new ConcurrentHashMap<>();

    @Override
    public String getOrCreateCode(String longUrl, Supplier<String> codeFactory) {
        return urlToCode.computeIfAbsent(longUrl, u -> {
            String code = codeFactory.get();
            codeToUrl.put(code, u);
            return code;
        });
    }

    @Override
    public boolean reserveAlias(String alias, String longUrl) {
        return codeToUrl.putIfAbsent(alias, longUrl) == null;
    }

    @Override
    public Optional<String> findUrl(String code) {
        return Optional.ofNullable(codeToUrl.get(code));
    }

    @Override
    public int size() {
        return codeToUrl.size();
    }
}
