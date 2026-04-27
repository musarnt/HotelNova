package com.hotelnova.exception;

/**
 * Thrown when trying to check out without a prior check-in.
 * Business rule: a guest cannot check out if the reservation
 * is not in CHECKED_IN status.
 */
public class InvalidCheckoutException extends RuntimeException {
    public InvalidCheckoutException(String message) {
        super(message);
    }
}