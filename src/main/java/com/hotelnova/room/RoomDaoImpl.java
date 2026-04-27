package com.hotelnova.room;

import com.hotelnova.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDaoImpl implements RoomDao {

    @Override
    public void save(Room room) {
        String sql = "INSERT INTO rooms (room_number, type, capacity, price_per_night, status, is_active) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getType().name());
            ps.setInt(3, room.getCapacity());
            ps.setDouble(4, room.getPricePerNight());
            ps.setString(5, room.getStatus().name());
            ps.setBoolean(6, room.isActive());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    room.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving room: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Room room) {
        String sql = "UPDATE rooms SET room_number = ?, type = ?, capacity = ?, price_per_night = ?, status = ?, is_active = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, room.getRoomNumber());
            ps.setString(2, room.getType().name());
            ps.setInt(3, room.getCapacity());
            ps.setDouble(4, room.getPricePerNight());
            ps.setString(5, room.getStatus().name());
            ps.setBoolean(6, room.isActive());
            ps.setInt(7, room.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating room: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Room> findById(int id) {
        String sql = "SELECT * FROM rooms WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRoom(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding room by id: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Room> findByRoomNumber(String roomNumber) {
        String sql = "SELECT * FROM rooms WHERE room_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRoom(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding room by number: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Room> findAll() {
        String sql = "SELECT * FROM rooms ORDER BY room_number";
        List<Room> rooms = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing rooms: " + e.getMessage(), e);
        }
        return rooms;
    }

    @Override
    public List<Room> findByType(RoomType type) {
        String sql = "SELECT * FROM rooms WHERE type = ? ORDER BY room_number";
        List<Room> rooms = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapResultSetToRoom(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error filtering rooms by type: " + e.getMessage(), e);
        }
        return rooms;
    }

    @Override
    public List<Room> findByStatus(RoomStatus status) {
        String sql = "SELECT * FROM rooms WHERE status = ? ORDER BY room_number";
        List<Room> rooms = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapResultSetToRoom(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error filtering rooms by status: " + e.getMessage(), e);
        }
        return rooms;
    }

    @Override
    public void updateStatus(int id, RoomStatus status) {
        String sql = "UPDATE rooms SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating room status: " + e.getMessage(), e);
        }
    }

    /**
     * Transactional updateStatus — uses an externally provided Connection.
     * Called by ReservationService during check-in and check-out transactions
     * so that both the reservation and room updates share the same connection and commit.
     * @param id the room ID to update
     * @param status the new room status
     * @param conn the active transactional connection provided by the service layer
     */
    @Override
    public void updateStatus(int id, RoomStatus status, Connection conn) throws SQLException {
        String sql = "UPDATE rooms SET status = ? WHERE id = ?";

        // Not try-with-resources — caller owns the connection and manages its lifecycle
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, status.name());
        ps.setInt(2, id);
        ps.executeUpdate();
        ps.close();
    }

    @Override
    public void updateActiveStatus(int id, boolean active) {
        String sql = "UPDATE rooms SET is_active = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating room active status: " + e.getMessage(), e);
        }
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        return new Room(
                rs.getInt("id"),
                rs.getString("room_number"),
                RoomType.valueOf(rs.getString("type")),
                rs.getInt("capacity"),
                rs.getDouble("price_per_night"),
                RoomStatus.valueOf(rs.getString("status")),
                rs.getBoolean("is_active"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}