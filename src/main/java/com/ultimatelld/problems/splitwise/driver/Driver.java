package com.ultimatelld.problems.splitwise.driver;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.splitwise.entity.UserId;
import com.ultimatelld.problems.splitwise.service.BalanceSheet;
import com.ultimatelld.problems.splitwise.service.ExpenseService;
import com.ultimatelld.problems.splitwise.strategy.EqualSplit;
import com.ultimatelld.problems.splitwise.strategy.PercentSplit;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Composition root + concurrent expense recording for Splitwise.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        UserId alice = UserId.of("alice"), bob = UserId.of("bob"),
                carol = UserId.of("carol"), dave = UserId.of("dave");

        BalanceSheet sheet = new BalanceSheet();
        ExpenseService service = new ExpenseService(sheet);

        // (1) alice pays 400.00 split equally among all four -> each owes 100 to alice
        service.addExpense(alice, Money.of(400_00), List.of(alice, bob, carol, dave), new EqualSplit());

        // (2) bob pays 90.00 split equally between bob & carol -> carol owes 45 to bob
        service.addExpense(bob, Money.of(90_00), List.of(bob, carol), new EqualSplit());

        // (3) carol pays 200.00 split 50/30/20 among carol/dave/alice
        service.addExpense(carol, Money.of(200_00), List.of(carol, dave, alice),
                new PercentSplit(Map.of(carol, 50, dave, 30, alice, 20)));

        System.out.println("Net balances (+owed / -owes):");
        for (UserId u : List.of(alice, bob, carol, dave)) {
            System.out.println("  " + u.value() + " = " + format(sheet.netBalanceMinor(u)));
        }
        System.out.println("Sum of all net balances = " + sheet.totalNetMinor() + " (must be 0)");
        System.out.println("Outstanding pairwise debts: " + sheet.outstandingDebts());
        System.out.println("Minimum settlements: " + sheet.simplifiedSettlements());

        // (4) Concurrency: many threads record equal-split expenses simultaneously; money conserved.
        BalanceSheet sheet2 = new BalanceSheet();
        ExpenseService svc2 = new ExpenseService(sheet2);
        int threads = 16, perThread = 1000;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        List<UserId> everyone = List.of(alice, bob, carol, dave);
        for (int t = 0; t < threads; t++) {
            UserId payer = everyone.get(t % everyone.size());
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        svc2.addExpense(payer, Money.of(40_00), everyone, new EqualSplit());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        pool.shutdownNow();
        System.out.println("[Concurrency] " + (threads * perThread) + " expenses recorded concurrently -> "
                + "sum of net balances = " + sheet2.totalNetMinor() + " (must be 0 — money conserved)");
    }

    private static String format(long minor) {
        return String.format("%d.%02d", minor / 100, Math.abs(minor % 100));
    }
}
