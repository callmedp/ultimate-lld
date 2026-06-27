# Pub-Sub — In-Memory Pub-Sub Messaging

**What this package is:** An in-memory publish/subscribe message broker with topics, partitions,
consumer groups, and per-group offset tracking.

Full walkthrough: [`docs/scenario-e-pubsub.md`](../../../../../../../docs/scenario-e-pubsub.md)

Run it:
```bash
./gradlew run -Pdriver=com.ultimatelld.problems.pubsub.driver.Driver
```

## Layout

| Subpackage | What it does |
|---|---|
| `core` | `Message`, `Subscriber`, and strongly-typed `TopicName` / `ConsumerGroupId`. |
| `broker` | `Broker` (entry point), `Topic`, `Partition`, `ConsumerGroup` — topic partitioning + consumer-group offsets for delivery guarantees. |
| `driver` | Composition root: publishes to topics and shows concurrent consumers receiving messages per group offset. |
