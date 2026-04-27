package com.hotelnova.exception;

/**
 * Thrown when trying to create a reservation for an inactive guest.
 * Business rule: only active guests can make reservations.
 */
public class InactiveGuestException extends RuntimeException {
    public InactiveGuestException(String message) {
        super(message);
    }
}