package com.hotelnova.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.sql.Connection;
import java.sql.SQLException;

public interface ReservationDao {

    void save(Reservation reservation);

    void update(Reservation reservation);
    void update(Reservation reservation, Connection conn) throws SQLException;

    Optional<Reservation> findById(int id);

    List<Reservation> findAll();

    List<Reservation> findActiveReservations();

    List<Reservation> findByGuestId(int guestId);

    List<Reservation> findByRoomId(int roomId);

    boolean hasOverlappingReservation(int roomId, LocalDate checkIn, LocalDate checkOut, int excludeId);

    Optional<Reservation> findActiveReservationByRoomId(int roomId);
}