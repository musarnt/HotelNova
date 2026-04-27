package com.hotelnova.exception;

/**
 * Thrown when trying to reserve a room that is not available.
 * A room might be occupied or under maintenance.
 */
public class RoomNotAvailableException extends RuntimeException {
    public RoomNotAvailableException(String message) {
        super(message);
    }
}