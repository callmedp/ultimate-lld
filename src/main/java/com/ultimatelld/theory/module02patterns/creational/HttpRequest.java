package com.ultimatelld.theory.module02patterns.creational;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * BUILDER pattern — a non-trivial IMMUTABLE object with required fields (method, url),
 * optional fields (headers, query params, body, timeout), and cross-field validation
 * concentrated in {@link Builder#build()}.
 * <p>
 * Why a builder and not a telescoping constructor: with this many optional fields a
 * positional constructor becomes an unreadable wall of nulls, and an all-setters mutable
 * object can be observed half-built. The builder keeps construction fluent and the result
 * fully validated and immutable.
 */
public final class HttpRequest {

    public enum Method { GET, POST, PUT, DELETE }

    private final Method method;
    private final String url;
    private final Map<String, String> headers;     // immutable copy
    private final Map<String, String> queryParams;  // immutable copy
    private final String body;                       // may be null (no body)
    private final int timeoutMillis;

    private HttpRequest(Builder b) {
        this.method = b.method;
        this.url = b.url;
        this.headers = Map.copyOf(b.headers);
        this.queryParams = Map.copyOf(b.queryParams);
        this.body = b.body;
        this.timeoutMillis = b.timeoutMillis;
    }

    public static Builder builder(Method method, String url) {
        return new Builder(method, url);
    }

    public Method method() {
        return method;
    }

    public String url() {
        return url;
    }

    public Map<String, String> headers() {
        return headers;
    }

    public Map<String, String> queryParams() {
        return queryParams;
    }

    public Optional<String> body() {
        return Optional.ofNullable(body);
    }

    public int timeoutMillis() {
        return timeoutMillis;
    }

    @Override
    public String toString() {
        return method + " " + url
                + " headers=" + headers
                + " query=" + queryParams
                + " body=" + (body == null ? "<none>" : body)
                + " timeoutMs=" + timeoutMillis;
    }

    /** Mutable builder; the product it yields is fully immutable. */
    public static final class Builder {

        private static final List<Method> BODYLESS = List.of(Method.GET, Method.DELETE);

        private final Method method;
        private final String url;
        private final java.util.LinkedHashMap<String, String> headers = new java.util.LinkedHashMap<>();
        private final java.util.LinkedHashMap<String, String> queryParams = new java.util.LinkedHashMap<>();
        private String body;
        private int timeoutMillis = 30_000; // sensible default

        private Builder(Method method, String url) {
            this.method = Objects.requireNonNull(method, "method");
            this.url = Objects.requireNonNull(url, "url");
            if (url.isBlank()) throw new IllegalArgumentException("url must not be blank");
        }

        public Builder header(String name, String value) {
            headers.put(Objects.requireNonNull(name, "name"), Objects.requireNonNull(value, "value"));
            return this;
        }

        public Builder query(String name, String value) {
            queryParams.put(Objects.requireNonNull(name, "name"), Objects.requireNonNull(value, "value"));
            return this;
        }

        public Builder body(String body) {
            this.body = Objects.requireNonNull(body, "body");
            return this;
        }

        public Builder timeoutMillis(int timeoutMillis) {
            if (timeoutMillis <= 0) throw new IllegalArgumentException("timeout must be > 0");
            this.timeoutMillis = timeoutMillis;
            return this;
        }

        /**
         * Single point of validation: enforce cross-field invariants that no individual
         * setter can see. Construction either yields a valid object or throws.
         */
        public HttpRequest build() {
            if (body != null && BODYLESS.contains(method)) {
                throw new IllegalStateException(method + " requests must not carry a body");
            }
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw new IllegalStateException("url must be absolute (http:// or https://): " + url);
            }
            return new HttpRequest(this);
        }
    }
}
