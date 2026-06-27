package com.ultimatelld.problems.taskscheduler.core;

/**
 * Execution priority. Lower {@code weight} means more urgent, so the ready queue's comparator
 * orders by ascending weight (HIGH first). Ties are broken FIFO by submission sequence.
 */
public enum Priority {
    HIGH(0),
    MEDIUM(1),
    LOW(2);

    private final int weight;

    Priority(int weight) {
        this.weight = weight;
    }

    public int weight() {
        return weight;
    }
}
