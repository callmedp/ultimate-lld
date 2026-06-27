package com.ultimatelld.problems.splitwise.entity;

import com.ultimatelld.common.Money;

import java.util.Map;
import java.util.Objects;

/**
 * An immutable record of who paid, how much, and each participant's computed share.
 * Invariant (checked at construction): the shares sum exactly to the total.
 */
public final class Expense {

    private final String id;
    private final UserId payer;
    private final Money total;
    private final Map<UserId, Money> shares;

    public Expense(String id, UserId payer, Money total, Map<UserId, Money> shares) {
        this.id = Objects.requireNonNull(id);
        this.payer = Objects.requireNonNull(payer);
        this.total = Objects.requireNonNull(total);
        this.shares = Map.copyOf(shares);
        long sum = this.shares.values().stream().mapToLong(Money::minor).sum();
        if (sum != total.minor()) {
            throw new IllegalArgumentException("shares (" + sum + ") must sum to total (" + total.minor() + ")");
        }
    }

    public String id() {
        return id;
    }

    public UserId payer() {
        return payer;
    }

    public Money total() {
        return total;
    }

    public Map<UserId, Money> shares() {
        return shares;
    }
}
