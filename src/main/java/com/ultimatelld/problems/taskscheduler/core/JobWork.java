package com.ultimatelld.problems.taskscheduler.core;

/** The unit of work a job performs. May throw — failures drive the retry/dead-letter logic. */
@FunctionalInterface
public interface JobWork {
    void execute() throws Exception;
}
