package com.hotelnova.room;

import com.hotelnova.exception.DuplicateRoomException;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Room management.
 * Contains business logic and validations:
 * - Unique room number validation
 * - Room status management
 * - Filtering by type and status
 *
 * Supports constructor injection of RoomDao for unit testing with mock DAOs.
 */
public class RoomService {

    private final RoomDao roomDao;

    /**
     * Default constructor — uses the real JDBC implementation.
     * Used by the application at runtime.
     */
    public RoomService() {
        this.roomDao = new RoomDaoImpl();
    }

    /**
     * Injectable constructor — accepts any RoomDao implementation.
     * Used by unit tests to inject a mock DAO without hitting the database.
     * @param roomDao the DAO implementation to use
     */
    public RoomService(RoomDao roomDao) {
        this.roomDao = roomDao;
    }

    /**
     * Registers a new room after validating that the room number is unique.
     * This is a key business rule: no two rooms can share the same number.
     * @param room the room to register
     * @throws DuplicateRoomException if room number already exists
     */
    public void register(Room room) {
        Optional<Room> existing = roomDao.findByRoomNumber(room.getRoomNumber());
        if (existing.isPresent()) {
            throw new DuplicateRoomException("Ya existe una habitación con el número: " + room.getRoomNumber());
        }
        roomDao.save(room);
    }

    /**
     * Updates an existing room's information.
     * If the room number changed, validates it's still unique.
     * @param room the room with updated data
     * @throws DuplicateRoomException if new room number conflicts with another room
     */
    public void update(Room room) {
        Optional<Room> existing = roomDao.findByRoomNumber(room.getRoomNumber());
        if (existing.isPresent() && existing.get().getId() != room.getId()) {
            throw new DuplicateRoomException("Ya existe otra habitación con el número: " + room.getRoomNumber());
        }
        roomDao.update(room);
    }

    public Room findById(int id) {
        return roomDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada con ID: " + id));
    }

    public Room findByRoomNumber(String roomNumber) {
        return roomDao.findByRoomNumber(roomNumber)
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada con número: " + roomNumber));
    }

    public List<Room> findAll() {
        return roomDao.findAll();
    }

    public List<Room> findByType(RoomType type) {
        return roomDao.findByType(type);
    }

    public List<Room> findByStatus(RoomStatus status) {
        return roomDao.findByStatus(status);
    }

    public void updateStatus(int id, RoomStatus status) {
        findById(id);
        roomDao.updateStatus(id, status);
    }

    public void toggleActiveStatus(int id, boolean active) {
        findById(id);
        roomDao.updateActiveStatus(id, active);
    }
}