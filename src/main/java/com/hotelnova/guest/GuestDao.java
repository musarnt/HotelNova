package com.hotelnova.guest;

import java.util.List;
import java.util.Optional;

public interface GuestDao {

    void save(Guest guest);

    void update(Guest guest);

    Optional<Guest> findById(int id);

    Optional<Guest> findByDocumentNumber(String documentNumber);

    List<Guest> findAll();

    List<Guest> findActiveGuests();

    void updateActiveStatus(int id, boolean active);
}