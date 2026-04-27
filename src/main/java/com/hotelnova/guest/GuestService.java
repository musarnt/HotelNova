package com.hotelnova.guest;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Guest management.
 * Contains business logic for guest registration, updates,
 * and active status management.
 */
public class GuestService {

    private final GuestDao guestDao;

    public GuestService() {
        this.guestDao = new GuestDaoImpl();
    }

    /**
     * Registers a new guest after validating document uniqueness.
     * Each guest must have a unique document number (cédula).
     * @param guest the guest to register
     * @throws RuntimeException if document number already exists
     */
    public void register(Guest guest) {
        // Validate unique document number
        Optional<Guest> existing = guestDao.findByDocumentNumber(guest.getDocumentNumber());
        if (existing.isPresent()) {
            throw new RuntimeException("Ya existe un huésped con el documento: " + guest.getDocumentNumber());
        }

        guestDao.save(guest);
    }

    /**
     * Updates an existing guest's information.
     * If document number changed, validates uniqueness.
     * @param guest the guest with updated data
     * @throws RuntimeException if new document number conflicts
     */
    public void update(Guest guest) {
        Optional<Guest> existing = guestDao.findByDocumentNumber(guest.getDocumentNumber());
        if (existing.isPresent() && existing.get().getId() != guest.getId()) {
            throw new RuntimeException("Ya existe otro huésped con el documento: " + guest.getDocumentNumber());
        }

        guestDao.update(guest);
    }

    /**
     * Finds a guest by their ID.
     * @param id the guest ID
     * @return the found Guest
     * @throws RuntimeException if no guest is found
     */
    public Guest findById(int id) {
        return guestDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Huésped no encontrado con ID: " + id));
    }

    /**
     * Finds a guest by their document number.
     * @param documentNumber the document number to search
     * @return the found Guest
     * @throws RuntimeException if no guest is found
     */
    public Guest findByDocumentNumber(String documentNumber) {
        return guestDao.findByDocumentNumber(documentNumber)
                .orElseThrow(() -> new RuntimeException("Huésped no encontrado con documento: " + documentNumber));
    }

    /**
     * Lists all guests in the system.
     * @return list of all guests
     */
    public List<Guest> findAll() {
        return guestDao.findAll();
    }

    /**
     * Lists only guests with active status.
     * Only active guests can make reservations.
     * @return list of active guests
     */
    public List<Guest> findActiveGuests() {
        return guestDao.findActiveGuests();
    }

    /**
     * Activates or deactivates a guest.
     * Inactive guests cannot create new reservations.
     * @param id the guest ID
     * @param active true to activate, false to deactivate
     */
    public void toggleActiveStatus(int id, boolean active) {
        findById(id);
        guestDao.updateActiveStatus(id, active);
    }
}