package com.hotelnova.reservation;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller layer for Reservation operations.
 * Bridges the JavaFX UI with ReservationService business logic.
 * Handles reservation creation, check-in, check-out, and queries.
 * All business validations are delegated to the service layer.
 */
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController() {
        this.reservationService = new ReservationService();
    }

    /**
     * Handles new reservation creation.
     * The service validates: active guest, available room, valid dates, no overlap.
     * @param guestId the guest making the reservation
     * @param roomId the room to reserve
     * @param checkInDate start date of the stay
     * @param checkOutDate end date of the stay
     */
    public void createReservation(int guestId, int roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        Reservation reservation = new Reservation(guestId, roomId, checkInDate, checkOutDate);
        reservationService.createReservation(reservation);
    }

    /**
     * Handles check-in process.
     * The service uses a JDBC transaction to atomically:
     * 1. Update reservation status to CHECKED_IN
     * 2. Update room status to OCCUPIED
     * @param reservationId the reservation to check in
     */
    public void checkIn(int reservationId) {
        reservationService.checkIn(reservationId);
    }

    /**
     * Handles check-out process.
     * The service uses a JDBC transaction to atomically:
     * 1. Calculate total cost (nights × price × (1 + IVA))
     * 2. Update reservation status to CHECKED_OUT
     * 3. Update room status to AVAILABLE
     * @param reservationId the reservation to check out
     */
    public void checkOut(int reservationId) {
        reservationService.checkOut(reservationId);
    }

    /**
     * Retrieves a reservation by ID.
     * @param id the reservation ID
     * @return the found Reservation
     */
    public Reservation findById(int id) {
        return reservationService.findById(id);
    }

    /**
     * Lists all reservations.
     * @return list of all reservations
     */
    public List<Reservation> findAll() {
        return reservationService.findAll();
    }

    /**
     * Lists active reservations (PENDING or CHECKED_IN).
     * @return list of active reservations
     */
    public List<Reservation> findActiveReservations() {
        return reservationService.findActiveReservations();
    }

    /**
     * Lists reservations by guest.
     * @param guestId the guest ID
     * @return list of that guest's reservations
     */
    public List<Reservation> findByGuestId(int guestId) {
        return reservationService.findByGuestId(guestId);
    }

    /**
     * Lists reservations by room.
     * @param roomId the room ID
     * @return list of that room's reservations
     */
    public List<Reservation> findByRoomId(int roomId) {
        return reservationService.findByRoomId(roomId);
    }

    /**
     * Calculates the estimated cost of a stay.
     * Useful for showing the guest a preview before confirming.
     * @param pricePerNight room's nightly rate
     * @param nights number of nights
     * @return total cost including IVA
     */
    public double calculateStayCost(double pricePerNight, long nights) {
        return reservationService.calculateStayCost(pricePerNight, nights);
    }
}