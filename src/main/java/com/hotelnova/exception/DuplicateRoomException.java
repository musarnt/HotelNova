package com.hotelnova.exception;

/**
 * Thrown when trying to register a room with a number that already exists.
 * Business rule: each room must have a unique room number.
 */
public class DuplicateRoomException extends RuntimeException {
    public DuplicateRoomException(String message) {
        super(message);
    }
}