package com.ultimatelld.problems.moviebooking.exception;

/** Thrown when payment fails during confirmation; the service rolls seats back to AVAILABLE. */
public class PaymentDeclinedException extends RuntimeException {
    public PaymentDeclinedException(String reason) {
        super("Payment declined: " + reason);
    }
}
