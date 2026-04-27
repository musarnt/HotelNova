package com.hotelnova.guest;

import com.hotelnova.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MySQL implementation of GuestDao.
 * Each method opens its own connection — no connection is shared across calls.
 */
public class GuestDaoImpl implements GuestDao {

    @Override
    public void save(Guest guest) {
        // created_at is omitted — the database sets it automatically on INSERT
        String sql = "INSERT INTO guests (first_name, last_name, document_number, phone, email, is_active) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, guest.getFirstName());
            ps.setString(2, guest.getLastName());
            ps.setString(3, guest.getDocumentNumber());
            ps.setString(4, guest.getPhone());
            ps.setString(5, guest.getEmail());
            ps.setBoolean(6, guest.isActive());
            ps.executeUpdate();

            // Write the generated id back into the object so the caller
            // has the key without needing a separate lookup.
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    guest.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving guest: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Guest guest) {
        String sql = "UPDATE guests SET first_name = ?, last_name = ?, document_number = ?, phone = ?, email = ?, is_active = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, guest.getFirstName());
            ps.setString(2, guest.getLastName());
            ps.setString(3, guest.getDocumentNumber());
            ps.setString(4, guest.getPhone());
            ps.setString(5, guest.getEmail());
            ps.setBoolean(6, guest.isActive());
            ps.setInt(7, guest.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating guest: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Guest> findById(int id) {
        String sql = "SELECT * FROM guests WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuest(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding guest by id: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Guest> findByDocumentNumber(String documentNumber) {
        String sql = "SELECT * FROM guests WHERE document_number = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, documentNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuest(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding guest by document: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Guest> findAll() {
        // Sorted alphabetically to make results predictable for the UI layer
        String sql = "SELECT * FROM guests ORDER BY last_name, first_name";
        List<Guest> guests = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                guests.add(mapResultSetToGuest(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing guests: " + e.getMessage(), e);
        }
        return guests;
    }

    @Override
    public List<Guest> findActiveGuests() {
        String sql = "SELECT * FROM guests WHERE is_active = TRUE ORDER BY last_name, first_name";
        List<Guest> guests = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                guests.add(mapResultSetToGuest(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing active guests: " + e.getMessage(), e);
        }
        return guests;
    }

    @Override
    public void updateActiveStatus(int id, boolean active) {
        String sql = "UPDATE guests SET is_active = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating guest active status: " + e.getMessage(), e);
        }
    }

    // Maps a raw database row to a Guest object.
    // Centralized here so any schema change only requires one fix.
    private Guest mapResultSetToGuest(ResultSet rs) throws SQLException {
        return new Guest(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("document_number"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getBoolean("is_active"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}