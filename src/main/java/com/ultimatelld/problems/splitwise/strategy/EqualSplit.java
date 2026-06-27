package com.ultimatelld.problems.splitwise.strategy;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.splitwise.entity.UserId;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Splits equally. Any indivisible remainder (in minor units) is distributed one unit at a time to
 * the first participants, so the shares always sum back to the exact total — no rounding leak.
 */
public final class EqualSplit implements SplitStrategy {
    @Override
    public Map<UserId, Money> computeShares(Money total, List<UserId> participants) {
        int n = participants.size();
        if (n == 0) throw new IllegalArgumentException("need at least one participant");
        long base = total.minor() / n;
        long remainder = total.minor() % n;
        Map<UserId, Money> shares = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            long share = base + (i < remainder ? 1 : 0);
            shares.put(participants.get(i), Money.of(share));
        }
        return shares;
    }

    @Override
    public String name() {
        return "EQUAL";
    }
}
