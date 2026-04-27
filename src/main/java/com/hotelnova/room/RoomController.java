package com.hotelnova.room;

import java.util.List;

/**
 * Controller layer for Room operations.
 * Bridges the JavaFX UI with RoomService business logic.
 * Handles room registration, updates, filtering, and status changes.
 */
public class RoomController {

    private final RoomService roomService;

    public RoomController() {
        this.roomService = new RoomService();
    }

    /**
     * Handles new room registration.
     * Creates a Room object with default status AVAILABLE and delegates to service.
     * @param roomNumber unique room identifier (e.g., "101", "202A")
     * @param type room type (SINGLE, DOUBLE, SUITE, DELUXE)
     * @param capacity maximum number of guests
     * @param pricePerNight nightly rate
     */
    public void register(String roomNumber, RoomType type, int capacity, double pricePerNight) {
        Room room = new Room(roomNumber, type, capacity, pricePerNight);
        roomService.register(room);
    }

    /**
     * Handles room information update.
     * @param room the room with modified fields
     */
    public void update(Room room) {
        roomService.update(room);
    }

    /**
     * Retrieves a room by ID.
     * @param id the room ID
     * @return the found Room
     */
    public Room findById(int id) {
        return roomService.findById(id);
    }

    /**
     * Retrieves a room by its unique number.
     * @param roomNumber the room number
     * @return the found Room
     */
    public Room findByRoomNumber(String roomNumber) {
        return roomService.findByRoomNumber(roomNumber);
    }

    /**
     * Lists all rooms in the system.
     * @return list of all rooms
     */
    public List<Room> findAll() {
        return roomService.findAll();
    }

    /**
     * Filters rooms by type (SINGLE, DOUBLE, SUITE, DELUXE).
     * @param type the type to filter by
     * @return filtered list of rooms
     */
    public List<Room> findByType(RoomType type) {
        return roomService.findByType(type);
    }

    /**
     * Filters rooms by status (AVAILABLE, OCCUPIED, MAINTENANCE).
     * @param status the status to filter by
     * @return filtered list of rooms
     */
    public List<Room> findByStatus(RoomStatus status) {
        return roomService.findByStatus(status);
    }

    /**
     * Updates the occupancy status of a room.
     * @param id the room ID
     * @param status the new status
     */
    public void updateStatus(int id, RoomStatus status) {
        roomService.updateStatus(id, status);
    }

    /**
     * Activates or deactivates a room.
     * @param id the room ID
     * @param active true to activate, false to deactivate
     */
    public void toggleActiveStatus(int id, boolean active) {
        roomService.toggleActiveStatus(id, active);
    }
}