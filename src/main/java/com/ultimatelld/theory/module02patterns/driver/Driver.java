package com.ultimatelld.theory.module02patterns.driver;

import com.ultimatelld.common.Money;
import com.ultimatelld.theory.module02patterns.behavioral.CountingObserver;
import com.ultimatelld.theory.module02patterns.behavioral.DiscountPricing;
import com.ultimatelld.theory.module02patterns.behavioral.PriceCalculator;
import com.ultimatelld.theory.module02patterns.behavioral.RegularPricing;
import com.ultimatelld.theory.module02patterns.behavioral.Subject;
import com.ultimatelld.theory.module02patterns.behavioral.SurgePricing;
import com.ultimatelld.theory.module02patterns.behavioral.VendingMachine;
import com.ultimatelld.theory.module02patterns.creational.Channel;
import com.ultimatelld.theory.module02patterns.creational.DoubleCheckedSingleton;
import com.ultimatelld.theory.module02patterns.creational.EnumSingleton;
import com.ultimatelld.theory.module02patterns.creational.FeatureRegistry;
import com.ultimatelld.theory.module02patterns.creational.HolderSingleton;
import com.ultimatelld.theory.module02patterns.creational.HttpRequest;
import com.ultimatelld.theory.module02patterns.creational.Notification;
import com.ultimatelld.theory.module02patterns.creational.NotificationFactory;
import com.ultimatelld.theory.module02patterns.structural.CheckoutFacade;
import com.ultimatelld.theory.module02patterns.structural.CompressionDecorator;
import com.ultimatelld.theory.module02patterns.structural.DataSource;
import com.ultimatelld.theory.module02patterns.structural.EncryptionDecorator;
import com.ultimatelld.theory.module02patterns.structural.InMemoryDataSource;
import com.ultimatelld.theory.module02patterns.structural.InventoryService;
import com.ultimatelld.theory.module02patterns.structural.PaymentGateway;
import com.ultimatelld.theory.module02patterns.structural.ShippingService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Composition Root for Module 2 — demonstrates EVERY pattern with printed output, and exercises
 * the thread-safe Singleton ({@link FeatureRegistry}) and {@link Subject} (Observer) from many
 * threads to show correctness under contention.
 */
public final class Driver {

    public static void main(String[] args) throws InterruptedException {
        section("CREATIONAL — Factory");
        demoFactory();

        section("CREATIONAL — Builder");
        demoBuilder();

        section("CREATIONAL — Singleton (holder / double-checked / enum)");
        demoSingletonIdioms();

        section("STRUCTURAL — Decorator");
        demoDecorator();

        section("STRUCTURAL — Facade");
        demoFacade();

        section("BEHAVIORAL — Strategy");
        demoStrategy();

        section("BEHAVIORAL — State");
        demoState();

        section("CONCURRENCY — thread-safe Singleton registry under load");
        demoConcurrentSingleton();

        section("CONCURRENCY — thread-safe Observer publish from many threads");
        demoConcurrentObserver();
    }

    private static void demoFactory() {
        NotificationFactory factory = new NotificationFactory();
        for (Channel c : Channel.values()) {
            Notification n = factory.create(c);
            System.out.println("  " + n.send("user-123", "Your order shipped"));
        }
    }

    private static void demoBuilder() {
        HttpRequest req = HttpRequest.builder(HttpRequest.Method.POST, "https://api.example.com/orders")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer xyz")
                .query("dryRun", "true")
                .body("{\"sku\":\"BOOK-001\",\"qty\":2}")
                .timeoutMillis(5_000)
                .build();
        System.out.println("  built: " + req);

        try {
            HttpRequest.builder(HttpRequest.Method.GET, "https://api.example.com/x")
                    .body("not allowed on GET")
                    .build();
        } catch (IllegalStateException e) {
            System.out.println("  validation rejected: " + e.getMessage());
        }
    }

    private static void demoSingletonIdioms() {
        boolean holderSame = HolderSingleton.getInstance() == HolderSingleton.getInstance();
        boolean dclSame = DoubleCheckedSingleton.getInstance() == DoubleCheckedSingleton.getInstance();
        boolean enumSame = EnumSingleton.INSTANCE == EnumSingleton.INSTANCE;
        System.out.println("  holder idiom    -> single instance? " + holderSame
                + " (" + HolderSingleton.getInstance().describe() + ")");
        System.out.println("  double-checked  -> single instance? " + dclSame
                + " (" + DoubleCheckedSingleton.getInstance().describe() + ")");
        System.out.println("  enum idiom      -> single instance? " + enumSame
                + " (" + EnumSingleton.INSTANCE.describe() + ")");
        System.out.println("  recommendation: prefer HOLDER or ENUM; double-checked is legacy/ceremony.");
    }

    private static void demoDecorator() {
        // wrap order matters: write goes compress -> encrypt -> store; read reverses it.
        DataSource base = new InMemoryDataSource();
        DataSource secured = new EncryptionDecorator(new CompressionDecorator(base));
        String payload = "the quick brown fox jumps over the lazy dog, repeatedly".repeat(3);
        secured.write(payload);
        System.out.println("  stored (compressed+encrypted, opaque): " + base.read().substring(0, 32) + "...");
        String roundTrip = secured.read();
        System.out.println("  round-trip equals original? " + roundTrip.equals(payload));
    }

    private static void demoFacade() {
        InventoryService inventory = new InventoryService(Map.of("BOOK-001", 5));
        CheckoutFacade checkout = new CheckoutFacade(inventory, new PaymentGateway(), new ShippingService());

        CheckoutFacade.Receipt receipt = checkout.checkout(
                "acct-9", "BOOK-001", 2, Money.of(499_00), "221B Baker Street");
        System.out.println("  one call did reserve+charge+ship -> " + receipt);
        System.out.println("  remaining stock: " + inventory.available("BOOK-001"));

        try {
            checkout.checkout("acct-9", "BOOK-001", 10, Money.of(499_00), "addr");
        } catch (IllegalStateException e) {
            System.out.println("  out-of-stock rejected: " + e.getMessage());
        }
    }

    private static void demoStrategy() {
        Money base = Money.of(100_00);
        int qty = 3;
        PriceCalculator calc = new PriceCalculator(new RegularPricing());
        System.out.println("  " + calc.strategyName() + " x" + qty + " = " + calc.quote(base, qty));
        calc = calc.withStrategy(new DiscountPricing(20));
        System.out.println("  " + calc.strategyName() + " x" + qty + " = " + calc.quote(base, qty));
        calc = calc.withStrategy(new SurgePricing(15000));
        System.out.println("  " + calc.strategyName() + " x" + qty + " = " + calc.quote(base, qty));
    }

    private static void demoState() {
        VendingMachine machine = new VendingMachine(25);
        System.out.println("  start: " + machine.currentState());
        machine.selectProduct(); // ignored - no money
        System.out.println("  after select-without-pay (ignored): " + machine.currentState());
        machine.insertCoin();
        System.out.println("  after coin: " + machine.currentState());
        machine.selectProduct();
        System.out.println("  after select: " + machine.currentState());
        machine.dispense();
        System.out.println("  after dispense: " + machine.currentState()
                + " (dispensed=" + machine.dispensedCount() + ")");
    }

    private static void demoConcurrentSingleton() throws InterruptedException {
        FeatureRegistry registry = FeatureRegistry.getInstance();
        registry.enable("dark-mode", true);
        registry.enable("beta-checkout", false);

        int threads = 32;
        int readsPerThread = 1_000;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int r = 0; r < readsPerThread; r++) {
                        registry.isEnabled("dark-mode");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await(5, TimeUnit.SECONDS);
        pool.shutdown();

        long expected = (long) threads * readsPerThread;
        System.out.println("  same instance from every thread? "
                + (FeatureRegistry.getInstance() == registry));
        System.out.println("  read counter = " + registry.readCount()
                + " (expected " + expected + ") -> "
                + (registry.readCount() == expected ? "CORRECT, no lost updates" : "MISMATCH"));
        System.out.println("  flags snapshot: " + registry.snapshot());
    }

    private static void demoConcurrentObserver() throws InterruptedException {
        Subject subject = new Subject();
        List<CountingObserver> observers = List.of(
                new CountingObserver("A"), new CountingObserver("B"), new CountingObserver("C"));
        observers.forEach(subject::subscribe);

        int publishers = 16;
        int eventsPerPublisher = 500;
        ExecutorService pool = Executors.newFixedThreadPool(publishers);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(publishers);

        for (int p = 0; p < publishers; p++) {
            final int id = p;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int e = 0; e < eventsPerPublisher; e++) {
                        subject.publish("evt-" + id + "-" + e);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await(5, TimeUnit.SECONDS);
        pool.shutdown();

        long totalEvents = (long) publishers * eventsPerPublisher;
        System.out.println("  published " + totalEvents + " events to " + subject.subscriberCount()
                + " observers from " + publishers + " threads");
        boolean allConsistent = observers.stream().allMatch(o -> o.received() == totalEvents);
        observers.forEach(o ->
                System.out.println("    observer " + o.id() + " received " + o.received()));
        System.out.println("  every observer saw every event? " + allConsistent
                + " | deliveries=" + subject.deliveryCount()
                + ", failures=" + subject.failureCount());
    }

    private static void section(String title) {
        System.out.println();
        System.out.println("=== " + title + " ===");
    }
}
