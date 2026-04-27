package com.hotelnova.reservation;

import com.hotelnova.config.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationDaoImpl implements ReservationDao {

    @Override
    public void save(Reservation reservation) {
        String sql = "INSERT INTO reservations (guest_id, room_id, check_in_date, check_out_date, status, total_cost) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, reservation.getGuestId());
            ps.setInt(2, reservation.getRoomId());
            ps.setDate(3, Date.valueOf(reservation.getCheckInDate()));
            ps.setDate(4, Date.valueOf(reservation.getCheckOutDate()));
            ps.setString(5, reservation.getStatus().name());
            ps.setDouble(6, reservation.getTotalCost());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    reservation.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving reservation: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Reservation reservation) {
        String sql = "UPDATE reservations SET guest_id = ?, room_id = ?, check_in_date = ?, check_out_date = ?, status = ?, total_cost = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, reservation.getGuestId());
            ps.setInt(2, reservation.getRoomId());
            ps.setDate(3, Date.valueOf(reservation.getCheckInDate()));
            ps.setDate(4, Date.valueOf(reservation.getCheckOutDate()));
            ps.setString(5, reservation.getStatus().name());
            ps.setDouble(6, reservation.getTotalCost());
            ps.setInt(7, reservation.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating reservation: " + e.getMessage(), e);
        }
    }

    /**
     * Transactional update — uses an externally provided Connection.
     * Called by ReservationService during check-in and check-out transactions
     * so that both the reservation and room updates share the same connection and commit.
     * @param reservation the reservation with updated status and cost
     * @param conn the active transactional connection provided by the service layer
     */
    @Override
    public void update(Reservation reservation, Connection conn) throws SQLException {
        String sql = "UPDATE reservations SET guest_id = ?, room_id = ?, check_in_date = ?, check_out_date = ?, status = ?, total_cost = ? WHERE id = ?";

        // Not try-with-resources — caller owns the connection and manages its lifecycle
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, reservation.getGuestId());
        ps.setInt(2, reservation.getRoomId());
        ps.setDate(3, Date.valueOf(reservation.getCheckInDate()));
        ps.setDate(4, Date.valueOf(reservation.getCheckOutDate()));
        ps.setString(5, reservation.getStatus().name());
        ps.setDouble(6, reservation.getTotalCost());
        ps.setInt(7, reservation.getId());
        ps.executeUpdate();
        ps.close();
    }

    @Override
    public Optional<Reservation> findById(int id) {
        String sql = "SELECT * FROM reservations WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reservation by id: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Reservation> findAll() {
        String sql = "SELECT * FROM reservations ORDER BY check_in_date DESC";
        List<Reservation> reservations = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing reservations: " + e.getMessage(), e);
        }
        return reservations;
    }

    @Override
    public List<Reservation> findActiveReservations() {
        String sql = "SELECT * FROM reservations WHERE status IN ('PENDING', 'CHECKED_IN') ORDER BY check_in_date";
        List<Reservation> reservations = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing active reservations: " + e.getMessage(), e);
        }
        return reservations;
    }

    @Override
    public List<Reservation> findByGuestId(int guestId) {
        String sql = "SELECT * FROM reservations WHERE guest_id = ? ORDER BY check_in_date DESC";
        List<Reservation> reservations = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, guestId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reservations by guest: " + e.getMessage(), e);
        }
        return reservations;
    }

    @Override
    public List<Reservation> findByRoomId(int roomId) {
        String sql = "SELECT * FROM reservations WHERE room_id = ? ORDER BY check_in_date DESC";
        List<Reservation> reservations = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding reservations by room: " + e.getMessage(), e);
        }
        return reservations;
    }

    @Override
    public boolean hasOverlappingReservation(int roomId, LocalDate checkIn, LocalDate checkOut, int excludeId) {
        String sql = "SELECT COUNT(*) FROM reservations WHERE room_id = ? AND id != ? AND status IN ('PENDING', 'CHECKED_IN') AND check_in_date < ? AND check_out_date > ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            ps.setInt(2, excludeId);
            ps.setDate(3, Date.valueOf(checkOut));
            ps.setDate(4, Date.valueOf(checkIn));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking overlapping reservations: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public Optional<Reservation> findActiveReservationByRoomId(int roomId) {
        String sql = "SELECT * FROM reservations WHERE room_id = ? AND status = 'CHECKED_IN' LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding active reservation by room: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getInt("id"),
                rs.getInt("guest_id"),
                rs.getInt("room_id"),
                rs.getDate("check_in_date").toLocalDate(),
                rs.getDate("check_out_date").toLocalDate(),
                ReservationStatus.valueOf(rs.getString("status")),
                rs.getDouble("total_cost"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}