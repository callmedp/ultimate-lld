package com.ultimatelld.problems.urlshortener.driver;

import com.ultimatelld.problems.urlshortener.core.SequenceIdGenerator;
import com.ultimatelld.problems.urlshortener.exception.AliasAlreadyExistsException;
import com.ultimatelld.problems.urlshortener.service.InMemoryUrlRepository;
import com.ultimatelld.problems.urlshortener.service.UrlShortenerService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Set;

/**
 * Composition root + concurrency demo for the URL shortener.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        UrlShortenerService service = new UrlShortenerService(
                new InMemoryUrlRepository(), new SequenceIdGenerator(1000));

        // ---- 1. Basic shorten/expand + idempotency ----
        String c1 = service.shorten("https://example.com/a");
        String c1again = service.shorten("https://example.com/a");
        String c2 = service.shorten("https://example.com/b");
        System.out.println("shorten a -> " + c1 + ", again -> " + c1again + " (idempotent, equal=" + c1.equals(c1again) + ")");
        System.out.println("shorten b -> " + c2 + " ; expand(" + c1 + ") = " + service.expand(c1));

        // ---- 2. Custom alias + collision ----
        String alias = service.shortenWithAlias("https://example.com/home", "home");
        System.out.println("custom alias '" + alias + "' -> " + service.expand("home"));
        try {
            service.shortenWithAlias("https://example.com/other", "home");
            System.out.println("ERROR: duplicate alias should be rejected");
        } catch (AliasAlreadyExistsException e) {
            System.out.println("duplicate alias rejected: " + e.getMessage());
        }

        // ---- 3. Concurrency: distinct URLs get unique codes; duplicates collapse ----
        int threads = 32, perThread = 2000, distinctUrls = 5000;
        Set<String> allCodes = ConcurrentHashMap.newKeySet();
        ConcurrentHashMap<String, String> codeForUrl = new ConcurrentHashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    var rnd = java.util.concurrent.ThreadLocalRandom.current();
                    for (int i = 0; i < perThread; i++) {
                        String url = "https://site/" + rnd.nextInt(distinctUrls);  // many duplicates
                        String code = service.shorten(url);
                        allCodes.add(code);
                        String prev = codeForUrl.putIfAbsent(url, code);
                        if (prev != null && !prev.equals(code)) {
                            throw new IllegalStateException("same URL got two codes!");
                        }
                    }
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
        System.out.println("[Concurrency] " + (threads * perThread) + " shorten calls over "
                + distinctUrls + " distinct URLs -> unique codes=" + allCodes.size()
                + ", distinct URLs seen=" + codeForUrl.size()
                + " (must be equal -> idempotent & collision-free)");
    }
}
