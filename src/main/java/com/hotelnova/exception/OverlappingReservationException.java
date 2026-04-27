package com.hotelnova.exception;

/**
 * Thrown when a new reservation overlaps with an existing one for the same room.
 * Uses range overlap logic: two date ranges overlap if start_A < end_B AND start_B < end_A.
 */
public class OverlappingReservationException extends RuntimeException {
    public OverlappingReservationException(String message) {
        super(message);
    }
}