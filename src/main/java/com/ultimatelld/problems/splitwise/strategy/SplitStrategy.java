package com.ultimatelld.problems.splitwise.strategy;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.splitwise.entity.UserId;

import java.util.List;
import java.util.Map;

/**
 * OCP split policy: given a total and the participants, return each participant's share. Equal,
 * exact, and percentage splits are separate implementations; a new scheme (e.g. share-weighted)
 * is a new class. Implementations MUST return shares that sum exactly to {@code total} (in minor
 * units) so no money is created or lost.
 */
public interface SplitStrategy {
    Map<UserId, Money> computeShares(Money total, List<UserId> participants);

    String name();
}
