package com.ultimatelld.problems.taskscheduler.core;

/** Lifecycle of a job: PENDING -> RUNNING -> {SUCCEEDED | (retry) PENDING | DEAD}. */
public enum JobState {
    PENDING,
    RUNNING,
    SUCCEEDED,
    DEAD
}
