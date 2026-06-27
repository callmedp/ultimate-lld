package com.ultimatelld.problems.pubsub.core;

/**
 * A consumer's handler. Invoked by a single delivery thread per assigned partition, so
 * implementations see per-partition messages strictly in offset order and need not be reentrant
 * for the same partition. Throwing signals a delivery failure (offset is not committed → redelivery).
 */
@FunctionalInterface
public interface Subscriber {
    void onMessage(Message message) throws Exception;
}
