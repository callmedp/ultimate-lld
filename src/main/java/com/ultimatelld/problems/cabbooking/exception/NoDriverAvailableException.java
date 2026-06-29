package com.ultimatelld.problems.cabbooking.exception;

/** Thrown when no driver is free to serve a ride request. */
public class NoDriverAvailableException extends RuntimeException {
    public NoDriverAvailableException() {
        super("No driver available for this request");
    }
}
