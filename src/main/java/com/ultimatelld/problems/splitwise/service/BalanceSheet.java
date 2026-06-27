package com.ultimatelld.problems.splitwise.service;

import com.ultimatelld.problems.splitwise.entity.UserId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe ledger of pairwise debts. A single {@link ReentrantLock} guards every mutation so an
 * expense's many balance updates are atomic and the global invariant (all net balances sum to zero)
 * always holds, even when expenses are recorded concurrently.
 *
 * <p>Invariant maintained on write: for any pair (a,b) at most one direction is non-zero — adding a
 * debt nets against the reverse debt first.
 */
public final class BalanceSheet {

    /** owed[a][b] = how much a still owes b (always >= 0). */
    private final Map<UserId, Map<UserId, Long>> owed = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    /** Record that {@code debtor} owes {@code creditor} {@code amountMinor}, netting any reverse debt. */
    public void addDebt(UserId debtor, UserId creditor, long amountMinor) {
        if (amountMinor <= 0 || debtor.equals(creditor)) return;
        lock.lock();
        try {
            long reverse = get(creditor, debtor);   // creditor currently owes debtor
            if (reverse >= amountMinor) {
                set(creditor, debtor, reverse - amountMinor);
            } else {
                long net = amountMinor - reverse;
                set(creditor, debtor, 0);
                set(debtor, creditor, get(debtor, creditor) + net);
            }
        } finally {
            lock.unlock();
        }
    }

    /** Net position of a user in minor units: positive = is owed money, negative = owes money. */
    public long netBalanceMinor(UserId user) {
        lock.lock();
        try {
            return netUnlocked(user);
        } finally {
            lock.unlock();
        }
    }

    /** Sum of all users' net balances — must always be exactly zero (money is conserved). */
    public long totalNetMinor() {
        lock.lock();
        try {
            long sum = 0;
            for (UserId u : allUsersUnlocked()) sum += netUnlocked(u);
            return sum;
        } finally {
            lock.unlock();
        }
    }

    /** A human-readable list of outstanding "X owes Y amount" entries. */
    public List<String> outstandingDebts() {
        lock.lock();
        try {
            List<String> out = new ArrayList<>();
            for (var e : owed.entrySet()) {
                for (var inner : e.getValue().entrySet()) {
                    if (inner.getValue() > 0) {
                        out.add(e.getKey().value() + " owes " + inner.getKey().value()
                                + " " + format(inner.getValue()));
                    }
                }
            }
            return out;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Minimum-cash-flow settlement: greedily match the biggest creditor with the biggest debtor.
     * Produces at most (users-1) transactions to settle everyone.
     */
    public List<String> simplifiedSettlements() {
        lock.lock();
        try {
            PriorityQueue<long[]> creditors = new PriorityQueue<>((x, y) -> Long.compare(y[1], x[1]));
            PriorityQueue<long[]> debtors = new PriorityQueue<>((x, y) -> Long.compare(y[1], x[1]));
            List<UserId> users = allUsersUnlocked();
            for (int i = 0; i < users.size(); i++) {
                long net = netUnlocked(users.get(i));
                if (net > 0) creditors.add(new long[]{i, net});
                else if (net < 0) debtors.add(new long[]{i, -net});
            }
            List<String> settlements = new ArrayList<>();
            while (!creditors.isEmpty() && !debtors.isEmpty()) {
                long[] c = creditors.poll();
                long[] d = debtors.poll();
                long pay = Math.min(c[1], d[1]);
                settlements.add(users.get((int) d[0]).value() + " pays " + users.get((int) c[0]).value()
                        + " " + format(pay));
                if (c[1] - pay > 0) creditors.add(new long[]{c[0], c[1] - pay});
                if (d[1] - pay > 0) debtors.add(new long[]{d[0], d[1] - pay});
            }
            return settlements;
        } finally {
            lock.unlock();
        }
    }

    // ---- helpers (caller holds the lock) ----

    private long netUnlocked(UserId user) {
        long owedToUser = 0;
        for (UserId other : owed.keySet()) {
            owedToUser += owed.getOrDefault(other, Map.of()).getOrDefault(user, 0L);
        }
        long userOwes = owed.getOrDefault(user, Map.of()).values().stream().mapToLong(Long::longValue).sum();
        return owedToUser - userOwes;
    }

    private List<UserId> allUsersUnlocked() {
        var set = new java.util.LinkedHashSet<UserId>();
        for (var e : owed.entrySet()) {
            set.add(e.getKey());
            set.addAll(e.getValue().keySet());
        }
        return new ArrayList<>(set);
    }

    private long get(UserId a, UserId b) {
        return owed.getOrDefault(a, Map.of()).getOrDefault(b, 0L);
    }

    private void set(UserId a, UserId b, long value) {
        Map<UserId, Long> inner = owed.computeIfAbsent(a, k -> new HashMap<>());
        if (value <= 0) inner.remove(b);
        else inner.put(b, value);
    }

    private String format(long minor) {
        return String.format("%d.%02d", minor / 100, Math.abs(minor % 100));
    }
}
