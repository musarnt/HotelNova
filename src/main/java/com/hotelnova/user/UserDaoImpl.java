package com.hotelnova.user;

import com.hotelnova.config.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MySQL implementation of UserDao.
 * Each method opens its own connection — no connection is shared across calls.
 */
public class UserDaoImpl implements UserDao {

    @Override
    public void save(User user) {
        // created_at is omitted — the database assigns it automatically on INSERT
        String sql = "INSERT INTO users (username, password, role, is_active) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            // Password is stored as received — in a real system this should be hashed
            ps.setString(2, user.getPassword());
            // Enums stored as string names for readability and flexibility in the database
            ps.setString(3, user.getRole().name());
            ps.setBoolean(4, user.isActive());
            ps.executeUpdate();

            // Retrieve generated ID after insert and assign it back to the entity
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user: " + e.getMessage(), e);
        }
    }

    @Override
    public void update(User user) {
        String sql = "UPDATE users SET username = ?, password = ?, role = ?, is_active = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRole().name());
            ps.setBoolean(4, user.isActive());
            ps.setInt(5, user.getId());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by id: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by username: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY username";
        List<User> users = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error listing users: " + e.getMessage(), e);
        }
        return users;
    }

    // Dedicated method for activation/deactivation — avoids modifying full user data unnecessarily
    @Override
    public void updateActiveStatus(int id, boolean active) {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, active);
            ps.setInt(2, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Error updating user active status: " + e.getMessage(), e);
        }
    }

    // Centralized mapping — any schema change only requires updating this method
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                UserRole.valueOf(rs.getString("role")),
                rs.getBoolean("is_active"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}