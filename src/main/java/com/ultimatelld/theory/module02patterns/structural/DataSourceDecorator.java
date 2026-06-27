package com.ultimatelld.theory.module02patterns.structural;

import java.util.Objects;

/**
 * DECORATOR — the abstract base decorator. It HAS-A wrapped {@link DataSource} and forwards by
 * default, so concrete decorators override only the behavior they add. Composition (wrapping),
 * not inheritance, is what extends behavior here — that is the whole point of the pattern.
 */
public abstract class DataSourceDecorator implements DataSource {

    protected final DataSource wrappee;

    protected DataSourceDecorator(DataSource wrappee) {
        this.wrappee = Objects.requireNonNull(wrappee, "wrappee");
    }

    @Override
    public void write(String data) {
        wrappee.write(data);
    }

    @Override
    public String read() {
        return wrappee.read();
    }
}
