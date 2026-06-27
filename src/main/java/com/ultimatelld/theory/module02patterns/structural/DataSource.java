package com.ultimatelld.theory.module02patterns.structural;

/**
 * DECORATOR pattern — the component interface.
 * <p>
 * Both the concrete component ({@code InMemoryDataSource}) and every decorator
 * ({@code CompressionDecorator}, {@code EncryptionDecorator}) implement this same interface, so
 * decorators are transparent: a caller cannot tell whether it holds a bare source or a stack of
 * wrappers. That transparency is what lets features be composed at runtime in any order.
 */
public interface DataSource {

    /** Persist the payload (a decorator may transform it on the way down). */
    void write(String data);

    /** Read the payload back (a decorator reverses its transform on the way up). */
    String read();
}
