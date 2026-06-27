package com.ultimatelld.theory.module02patterns.structural;

import java.util.Objects;

/** FACADE subsystem part — schedules a shipment and returns a tracking number. */
public final class ShippingService {

    public String schedule(String sku, int quantity, String address) {
        Objects.requireNonNull(sku, "sku");
        Objects.requireNonNull(address, "address");
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        return "TRACK-" + Integer.toHexString((sku + quantity + address).hashCode());
    }
}
