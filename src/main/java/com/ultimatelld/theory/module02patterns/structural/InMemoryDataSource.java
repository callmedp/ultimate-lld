package com.ultimatelld.theory.module02patterns.structural;

/**
 * DECORATOR — the concrete component being wrapped. Stands in for a file / blob store;
 * it simply round-trips whatever bytes it is handed.
 */
public final class InMemoryDataSource implements DataSource {

    private String storage = "";

    @Override
    public void write(String data) {
        this.storage = (data == null) ? "" : data;
    }

    @Override
    public String read() {
        return storage;
    }
}
