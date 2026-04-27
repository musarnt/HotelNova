package com.hotelnova.reservation;

import com.hotelnova.config.AppConfig;
import com.hotelnova.exception.*;
import com.hotelnova.guest.Guest;
import com.hotelnova.guest.GuestDao;
import com.hotelnova.guest.GuestDaoImpl;
import com.hotelnova.room.Room;
import com.hotelnova.room.RoomDao;
import com.hotelnova.room.RoomDaoImpl;
import com.hotelnova.room.RoomStatus;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Reservation management.
 * Handles all critical operations with full business validation:
 * - Reservation creation (validates guest, room, dates, overlap)
 * - Check-in and check-out using JDBC transactions
 * - Stay cost calculation: nights × price × (1 + IVA)
 *
 * Supports constructor injection for unit testing without a real database.
 */
public class ReservationService {

    private final ReservationDao reservationDao;
    private final RoomDao roomDao;
    private final GuestDao guestDao;

    /**
     * Default constructor — uses real JDBC implementations.
     * Used by the application at runtime.
     */
    public ReservationService() {
        this.reservationDao = new ReservationDaoImpl();
        this.roomDao = new RoomDaoImpl();
        this.guestDao = new GuestDaoImpl();
    }

    /**
     * Injectable constructor — accepts any DAO implementations.
     * Used by unit tests to inject mock DAOs without hitting the database.
     * @param reservationDao the reservation DAO to use
     * @param roomDao the room DAO to use
     * @param guestDao the guest DAO to use
     */
    public ReservationService(ReservationDao reservationDao, RoomDao roomDao, GuestDao guestDao) {
        this.reservationDao = reservationDao;
        this.roomDao = roomDao;
        this.guestDao = guestDao;
    }

    /**
     * Creates a new reservation after validating ALL business rules:
     * 1. Guest must exist and be active
     * 2. Room must exist and be AVAILABLE
     * 3. Check-in date must be before check-out date
     * 4. No overlapping reservations for the same room
     */
    public void createReservation(Reservation reservation) {
        // 1. Validate guest exists and is active
        Guest guest = guestDao.findById(reservation.getGuestId())
                .orElseThrow(() -> new RuntimeException("Huésped no encontrado."));

        if (!guest.isActive()) {
            throw new InactiveGuestException("El huésped " + guest.getFullName() + " no está activo.");
        }

        // 2. Validate room exists and is available
        Room room = roomDao.findById(reservation.getRoomId())
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada."));

        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new RoomNotAvailableException(
                    "La habitación " + room.getRoomNumber() + " no está disponible.");
        }

        // 3. Validate date range: check-in must be strictly before check-out
        if (!reservation.getCheckInDate().isBefore(reservation.getCheckOutDate())) {
            throw new InvalidDateRangeException(
                    "La fecha de check-in debe ser anterior a la fecha de check-out.");
        }

        // 4. Validate no overlapping reservations for this room
        // Range overlap: A.start < B.end AND B.start < A.end
        boolean hasOverlap = reservationDao.hasOverlappingReservation(
                reservation.getRoomId(),
                reservation.getCheckInDate(),
                reservation.getCheckOutDate(),
                0
        );

        if (hasOverlap) {
            throw new OverlappingReservationException(
                    "La habitación " + room.getRoomNumber() + " ya tiene una reserva en esas fechas.");
        }

        reservationDao.save(reservation);
    }

    /**
     * Processes check-in using a JDBC transaction.
     * Opens a dedicated connection and injects it into both DAOs,
     * ensuring reservation update and room status change share the same commit.
     * If either operation fails, both are rolled back atomically.
     */
    public void checkIn(int reservationId) {
        Reservation reservation = reservationDao.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada."));

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new RuntimeException("Solo se puede hacer check-in de reservas con estado PENDIENTE.");
        }

        // Open a dedicated connection for this transaction
        // try-with-resources guarantees the connection is closed after commit or rollback
        try (Connection conn = DriverManager.getConnection(
                AppConfig.getDbUrl(), AppConfig.getDbUser(), AppConfig.getDbPassword())) {

            conn.setAutoCommit(false);
            try {
                reservation.setStatus(ReservationStatus.CHECKED_IN);

                // Both DAOs receive the same connection — same transaction, same commit
                reservationDao.update(reservation, conn);
                roomDao.updateStatus(reservation.getRoomId(), RoomStatus.OCCUPIED, conn);

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Error durante el check-in: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en check-in: " + e.getMessage(), e);
        }
    }

    /**
     * Processes check-out using a JDBC transaction.
     * Opens a dedicated connection and injects it into both DAOs.
     * Atomically: calculates cost → updates reservation (CHECKED_OUT + totalCost) → room (AVAILABLE).
     * Cannot check out without a prior check-in (reservation must be CHECKED_IN).
     */
    public void checkOut(int reservationId) {
        Reservation reservation = reservationDao.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada."));

        // Cannot check out without a prior check-in
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new InvalidCheckoutException(
                    "No se puede hacer check-out sin un check-in previo.");
        }

        Room room = roomDao.findById(reservation.getRoomId())
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada."));

        // Calculate total cost before opening the transaction
        double iva = AppConfig.getIva();
        double totalCost = reservation.calculateCost(room.getPricePerNight(), iva);

        // Open a dedicated connection for this transaction
        try (Connection conn = DriverManager.getConnection(
                AppConfig.getDbUrl(), AppConfig.getDbUser(), AppConfig.getDbPassword())) {

            conn.setAutoCommit(false);
            try {
                reservation.setStatus(ReservationStatus.CHECKED_OUT);
                reservation.setTotalCost(totalCost);

                // Both DAOs receive the same connection — same transaction, same commit
                reservationDao.update(reservation, conn);
                roomDao.updateStatus(reservation.getRoomId(), RoomStatus.AVAILABLE, conn);

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Error durante el check-out: " + e.getMessage(), e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión en check-out: " + e.getMessage(), e);
        }
    }

    /**
     * Calculates the total cost of a stay.
     * Formula: nights × pricePerNight × (1 + IVA)
     * IVA is read from config.properties.
     */
    public double calculateStayCost(double pricePerNight, long nights) {
        double iva = AppConfig.getIva();
        double subtotal = nights * pricePerNight;
        return subtotal + (subtotal * iva);
    }

    public Reservation findById(int id) {
        return reservationDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + id));
    }

    public List<Reservation> findAll() {
        return reservationDao.findAll();
    }

    public List<Reservation> findActiveReservations() {
        return reservationDao.findActiveReservations();
    }

    public List<Reservation> findByGuestId(int guestId) {
        return reservationDao.findByGuestId(guestId);
    }

    public List<Reservation> findByRoomId(int roomId) {
        return reservationDao.findByRoomId(roomId);
    }
}