package com.ultimatelld.problems.splitwise.service;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.splitwise.entity.Expense;
import com.ultimatelld.problems.splitwise.entity.UserId;
import com.ultimatelld.problems.splitwise.strategy.SplitStrategy;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Orchestrates expense recording: it computes shares via the injected {@link SplitStrategy}, builds
 * an immutable {@link Expense} (which self-validates that shares sum to the total), then posts the
 * resulting debts into the thread-safe {@link BalanceSheet}. Holds no business rules itself.
 */
public final class ExpenseService {

    private final BalanceSheet balanceSheet;
    private final AtomicLong expenseSeq = new AtomicLong();

    public ExpenseService(BalanceSheet balanceSheet) {
        this.balanceSheet = Objects.requireNonNull(balanceSheet);
    }

    public Expense addExpense(UserId payer, Money total, List<UserId> participants, SplitStrategy strategy) {
        Map<UserId, Money> shares = strategy.computeShares(total, participants);
        Expense expense = new Expense("exp-" + expenseSeq.incrementAndGet(), payer, total, shares);

        // Every participant other than the payer owes the payer their share.
        for (Map.Entry<UserId, Money> e : shares.entrySet()) {
            if (!e.getKey().equals(payer)) {
                balanceSheet.addDebt(e.getKey(), payer, e.getValue().minor());
            }
        }
        return expense;
    }

    public BalanceSheet balanceSheet() {
        return balanceSheet;
    }
}
