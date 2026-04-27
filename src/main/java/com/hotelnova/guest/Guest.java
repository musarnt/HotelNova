package com.hotelnova.guest;

import java.time.LocalDateTime;

/**
 * Represents a hotel guest who can make reservations.
 *
 * Guests are never hard-deleted — they are deactivated via the active flag
 * to preserve reservation history and audit trails.
 */
public class Guest {

    private int id;
    private String firstName;
    private String lastName;
    private String documentNumber;
    private String phone;
    private String email;
    private boolean active;
    private LocalDateTime createdAt;
    public Guest() {
    }

    /**
     * Used when registering a new guest.
     * active defaults to true — a newly registered guest is always active.
     * id and createdAt are intentionally omitted; the database assigns them on INSERT.
     */
    public Guest(String firstName, String lastName, String documentNumber, String phone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.documentNumber = documentNumber;
        this.phone = phone;
        this.email = email;
        this.active = true;
    }

    /**
     * Used when reconstructing a guest from a database row.
     * All fields are provided because they were already persisted.
     */
    public Guest(int id, String firstName, String lastName, String documentNumber,
                 String phone, String email, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.documentNumber = documentNumber;
        this.phone = phone;
        this.email = email;
        this.active = active;
        this.createdAt = createdAt;
    }

    // Convenience method — avoids scattering string concatenation across the codebase.
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // toString intentionally omits contact details (phone, email) to avoid
    // leaking PII into logs.
    @Override
    public String toString() {
        return String.format("Guest{id=%d, name='%s', document='%s', active=%s}",
                id, getFullName(), documentNumber, active);
    }

    // --- Getters and Setters ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}