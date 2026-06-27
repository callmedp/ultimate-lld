package com.ultimatelld.theory.module02patterns.structural;

import com.ultimatelld.common.Money;

import java.util.Objects;

/** FACADE subsystem part — charges a card and returns a transaction reference. */
public final class PaymentGateway {

    /** @return an opaque transaction id; throws if the amount is non-positive. */
    public String charge(String account, Money amount) {
        Objects.requireNonNull(account, "account");
        Objects.requireNonNull(amount, "amount");
        if (amount.minor() <= 0) throw new IllegalArgumentException("amount must be positive");
        return "TXN-" + Integer.toHexString((account + amount.minor()).hashCode());
    }
}
