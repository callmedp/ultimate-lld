package com.ultimatelld.theory.module02patterns.structural;

import com.ultimatelld.common.Money;

import java.util.Objects;

/**
 * FACADE pattern — collapses a multi-step subsystem (reserve stock, charge payment, schedule
 * shipping) behind ONE simple {@link #checkout} call. The client states intent once; it never
 * learns the orchestration order, the rollback rule, or the existence of three services (DIP at
 * the boundary). The facade adds a coordination policy the subsystem parts deliberately lack.
 * <p>
 * Note: the facade does not hide the subsystem — those classes stay public and usable directly.
 * It offers a convenient default path, which is exactly the pattern's intent.
 */
public final class CheckoutFacade {

    private final InventoryService inventory;
    private final PaymentGateway payment;
    private final ShippingService shipping;

    public CheckoutFacade(InventoryService inventory, PaymentGateway payment, ShippingService shipping) {
        this.inventory = Objects.requireNonNull(inventory, "inventory");
        this.payment = Objects.requireNonNull(payment, "payment");
        this.shipping = Objects.requireNonNull(shipping, "shipping");
    }

    /** Immutable result of a successful checkout. */
    public record Receipt(String transactionId, String trackingNumber) {
        public Receipt {
            Objects.requireNonNull(transactionId, "transactionId");
            Objects.requireNonNull(trackingNumber, "trackingNumber");
        }
    }

    /**
     * One call orchestrates the whole flow. If a later step fails after stock was reserved,
     * the facade compensates (releases the reservation) so the subsystem is left consistent.
     *
     * @throws IllegalStateException if stock is insufficient.
     */
    public Receipt checkout(String account, String sku, int quantity, Money unitPrice, String address) {
        if (!inventory.reserve(sku, quantity)) {
            throw new IllegalStateException("Out of stock for " + sku + " (need " + quantity + ")");
        }
        try {
            Money total = unitPrice.multiply(quantity);
            String txn = payment.charge(account, total);
            String tracking = shipping.schedule(sku, quantity, address);
            return new Receipt(txn, tracking);
        } catch (RuntimeException e) {
            inventory.release(sku, quantity); // compensating action on partial failure
            throw e;
        }
    }
}
