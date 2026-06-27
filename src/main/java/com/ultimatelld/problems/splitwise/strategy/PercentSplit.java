package com.ultimatelld.problems.splitwise.strategy;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.splitwise.entity.UserId;
import com.ultimatelld.problems.splitwise.exception.InvalidSplitException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Caller specifies each participant's percentage (must sum to 100). Shares are the floor of the
 * percentage of the total; any leftover minor units from flooring are handed out one at a time to
 * the first participants, so the shares sum to the exact total.
 */
public final class PercentSplit implements SplitStrategy {

    private final Map<UserId, Integer> percentages;

    public PercentSplit(Map<UserId, Integer> percentages) {
        this.percentages = Map.copyOf(percentages);
        int sum = percentages.values().stream().mapToInt(Integer::intValue).sum();
        if (sum != 100) throw new InvalidSplitException("percentages must sum to 100, got " + sum);
    }

    @Override
    public Map<UserId, Money> computeShares(Money total, List<UserId> participants) {
        Map<UserId, Money> shares = new LinkedHashMap<>();
        long assigned = 0;
        for (UserId p : participants) {
            Integer pct = percentages.get(p);
            if (pct == null) throw new InvalidSplitException("no percentage for " + p.value());
            long share = total.minor() * pct / 100;
            shares.put(p, Money.of(share));
            assigned += share;
        }
        // distribute flooring remainder one minor unit at a time
        long remainder = total.minor() - assigned;
        for (UserId p : participants) {
            if (remainder <= 0) break;
            shares.put(p, shares.get(p).add(Money.of(1)));
            remainder--;
        }
        return shares;
    }

    @Override
    public String name() {
        return "PERCENT";
    }
}
