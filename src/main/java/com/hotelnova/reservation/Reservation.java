package com.hotelnova.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Represents a hotel reservation linking a guest to a room for a date range.
 *
 * Uses LocalDate (not LocalDateTime) for check-in/out because the exact time
 * is governed by hotel policy (see AppConfig), not stored per reservation.
 */
public class Reservation {

    private int id;
    private int guestId;
    private int roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    // Drives business logic throughout the reservation lifecycle —
    // see ReservationStatus for the valid transitions.
    private ReservationStatus status;

    // Stored after confirmation; 0 until calculateCost() is called and persisted.
    private double totalCost;

    private LocalDateTime createdAt;

    // Required for JDBC row mapping.
    public Reservation() {
    }

    /**
     * Used when creating a new reservation.
     * Status starts as PENDING until a staff member confirms availability.
     * totalCost is 0 until the room price is resolved and persisted.
     */
    public Reservation(int guestId, int roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        this.guestId = guestId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = ReservationStatus.PENDING;
        this.totalCost = 0;
    }

    // Used when reconstructing a reservation from a database row.
    public Reservation(int id, int guestId, int roomId, LocalDate checkInDate, LocalDate checkOutDate,
                       ReservationStatus status, double totalCost, LocalDateTime createdAt) {
        this.id = id;
        this.guestId = guestId;
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = status;
        this.totalCost = totalCost;
        this.createdAt = createdAt;
    }

    // checkOut is exclusive — a guest checking in on the 5th and out on the 6th stays 1 night.
    public long calculateNights() {
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    // iva is passed in rather than read from AppConfig directly
    // to keep this class free of static dependencies and easier to test.
    public double calculateCost(double pricePerNight, double iva) {
        long nights = calculateNights();
        double subtotal = nights * pricePerNight;
        return subtotal + (subtotal * iva);
    }

    // --- Getters and Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getGuestId() { return guestId; }
    public void setGuestId(int guestId) { this.guestId = guestId; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return String.format("Reservation{id=%d, guestId=%d, roomId=%d, checkIn=%s, checkOut=%s, status=%s, cost=%.2f}",
                id, guestId, roomId, checkInDate, checkOutDate, status, totalCost);
    }
}