package com.hotelnova.room;

import java.time.LocalDateTime;

/**
 * Represents a physical hotel room.
 *
 * Has two independent flags:
 *   - status: operational state (AVAILABLE, OCCUPIED, MAINTENANCE) — changes frequently
 *   - active: whether the room exists in the system — false means soft-deleted
 */
public class Room {

    private int id;
    private String roomNumber;
    private RoomType type;
    private int capacity;
    private double pricePerNight;
    private RoomStatus status;
    private boolean active;
    private LocalDateTime createdAt;

    // Required for JDBC row mapping.
    public Room() {
    }

    // Used when registering a new room.
    // Status defaults to AVAILABLE and active to true — a new room is ready to book.
    public Room(String roomNumber, RoomType type, int capacity, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
        this.status = RoomStatus.AVAILABLE;
        this.active = true;
    }

    // Used when reconstructing a room from a database row.
    public Room(int id, String roomNumber, RoomType type, int capacity, double pricePerNight,
                RoomStatus status, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.type = type;
        this.capacity = capacity;
        this.pricePerNight = pricePerNight;
        this.status = status;
        this.active = active;
        this.createdAt = createdAt;
    }

    // --- Getters and Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }

    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("Room{id=%d, number='%s', type=%s, capacity=%d, price=%.2f, status=%s, active=%s}",
                id, roomNumber, type, capacity, pricePerNight, status, active);
    }
}