package com.hotelnova.guest;

import java.util.List;

/**
 * Controller layer for Guest operations.
 * Bridges the JavaFX UI with GuestService business logic.
 * Handles guest registration, updates, and active status management.
 */
public class GuestController {

    private final GuestService guestService;

    public GuestController() {
        this.guestService = new GuestService();
    }

    /**
     * Handles new guest registration.
     * Creates a Guest with default active=true and delegates to service.
     * @param firstName guest's first name
     * @param lastName guest's last name
     * @param documentNumber unique identification document (cédula)
     * @param phone contact phone number
     * @param email contact email
     */
    public void register(String firstName, String lastName, String documentNumber, String phone, String email) {
        Guest guest = new Guest(firstName, lastName, documentNumber, phone, email);
        guestService.register(guest);
    }

    /**
     * Handles guest information update.
     * @param guest the guest with modified fields
     */
    public void update(Guest guest) {
        guestService.update(guest);
    }

    /**
     * Retrieves a guest by ID.
     * @param id the guest ID
     * @return the found Guest
     */
    public Guest findById(int id) {
        return guestService.findById(id);
    }

    /**
     * Retrieves a guest by document number.
     * @param documentNumber the document to search
     * @return the found Guest
     */
    public Guest findByDocumentNumber(String documentNumber) {
        return guestService.findByDocumentNumber(documentNumber);
    }

    /**
     * Lists all guests in the system.
     * @return list of all guests
     */
    public List<Guest> findAll() {
        return guestService.findAll();
    }

    /**
     * Lists only active guests.
     * Used when creating reservations — only active guests can reserve.
     * @return list of active guests
     */
    public List<Guest> findActiveGuests() {
        return guestService.findActiveGuests();
    }

    /**
     * Activates or deactivates a guest.
     * @param id the guest ID
     * @param active true to activate, false to deactivate
     */
    public void toggleActiveStatus(int id, boolean active) {
        guestService.toggleActiveStatus(id, active);
    }
}