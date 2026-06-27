package com.ultimatelld.theory.module03concurrency.inventory;

/** Reserve one unit of stock; returns true if a unit was successfully reserved. */
public interface InventoryService {
    boolean reserve();

    int remaining();

    String label();
}
