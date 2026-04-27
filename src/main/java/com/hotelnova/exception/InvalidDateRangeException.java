package com.hotelnova.exception;

/**
 * Thrown when check-in date is not before check-out date.
 * Business rule: check-in must be strictly earlier than check-out.
 */
public class InvalidDateRangeException extends RuntimeException {
    public InvalidDateRangeException(String message) {
        super(message);
    }
}