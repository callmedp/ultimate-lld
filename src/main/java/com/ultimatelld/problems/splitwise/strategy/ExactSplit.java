package com.ultimatelld.problems.splitwise.strategy;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.splitwise.entity.UserId;
import com.ultimatelld.problems.splitwise.exception.InvalidSplitException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Caller specifies the exact amount each participant owes; validated to sum to the total. */
public final class ExactSplit implements SplitStrategy {

    private final Map<UserId, Money> exactShares;

    public ExactSplit(Map<UserId, Money> exactShares) {
        this.exactShares = Map.copyOf(exactShares);
    }

    @Override
    public Map<UserId, Money> computeShares(Money total, List<UserId> participants) {
        long sum = 0;
        Map<UserId, Money> shares = new LinkedHashMap<>();
        for (UserId p : participants) {
            Money share = exactShares.get(p);
            if (share == null) throw new InvalidSplitException("no exact share provided for " + p.value());
            shares.put(p, share);
            sum += share.minor();
        }
        if (sum != total.minor()) {
            throw new InvalidSplitException("exact shares sum to " + sum + " but total is " + total.minor());
        }
        return shares;
    }

    @Override
    public String name() {
        return "EXACT";
    }
}
