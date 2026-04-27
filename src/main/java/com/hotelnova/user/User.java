package com.hotelnova.user;

import java.time.LocalDateTime;

/**
 * Represents a staff member who can log into the system.
 * Guests are a separate entity — see com.hotelnova.guest.Guest.
 */
public class User {

    private int id;
    private String username;

    // Stored as a hash — never the plain-text password.
    private String password;

    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;

    // Required for JDBC row mapping.
    public User() {
    }

    // Used when registering a new user.
    // active defaults to true — a newly created user can log in immediately.
    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = true;
    }

    // Used when reconstructing a user from a database row.
    public User(int id, String username, String password, UserRole role, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    // --- Getters and Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // toString omits password intentionally — even hashed, it shouldn't appear in logs.
    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', role=%s, active=%s}", id, username, role, active);
    }
}