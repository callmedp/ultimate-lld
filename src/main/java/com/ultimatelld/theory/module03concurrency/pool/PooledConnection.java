package com.ultimatelld.theory.module03concurrency.pool;

/** A stand-in for an expensive resource (e.g. a DB connection) managed by the pool. */
public final class PooledConnection {

    private final int id;

    public PooledConnection(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public String execute(String query) {
        return "conn-" + id + " ran [" + query + "]";
    }

    @Override
    public String toString() {
        return "conn-" + id;
    }
}
