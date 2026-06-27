package com.ultimatelld.problems.splitwise.exception;

/** Thrown when a split's shares don't reconcile with the expense total or participants. */
public class InvalidSplitException extends RuntimeException {
    public InvalidSplitException(String message) {
        super(message);
    }
}
