package com.hotelnova;

import com.hotelnova.exception.DuplicateRoomException;
import com.hotelnova.room.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for RoomService business logic.
 * Uses a mock DAO to avoid hitting the real database —
 * tests only validate business rules, not persistence.
 */
class RoomServiceTest {

    private RoomService roomService;

    /**
     * Creates a RoomService with a fake (mock) DAO before each test.
     * This isolates the service logic from the database entirely.
     */
    @BeforeEach
    void setUp() {
        roomService = new RoomService(new MockRoomDao());
    }

    /**
     * Test 1: registering a room with a number that already exists must throw DuplicateRoomException.
     * Business rule: each room must have a unique room number.
     */
    @Test
    void register_duplicateRoomNumber_throwsDuplicateRoomException() {
        Room existing = new Room("101", RoomType.SINGLE, 1, 100000);

        // First registration — should succeed
        roomService.register(existing);

        // Second registration with same number — must throw
        Room duplicate = new Room("101", RoomType.DOUBLE, 2, 150000);
        assertThrows(DuplicateRoomException.class, () -> roomService.register(duplicate));
    }

    /**
     * Test 2: registering a room with a unique number must succeed without exceptions.
     */
    @Test
    void register_uniqueRoomNumber_succeeds() {
        Room room = new Room("202", RoomType.DOUBLE, 2, 200000);
        assertDoesNotThrow(() -> roomService.register(room));
    }

    /**
     * Test 3: a room with AVAILABLE status is available for reservation.
     */
    @Test
    void findByStatus_available_returnsAvailableRooms() {
        roomService.register(new Room("301", RoomType.SUITE, 3, 350000));

        var available = roomService.findByStatus(RoomStatus.AVAILABLE);
        assertFalse(available.isEmpty(), "Should return at least one available room");
    }

    // -------------------------------------------------------------------------
    // Simple in-memory mock DAO — no database needed for unit tests
    // Stores rooms in a list and simulates the real DAO behavior
    // -------------------------------------------------------------------------
    static class MockRoomDao implements RoomDao {

        private final java.util.List<Room> rooms = new java.util.ArrayList<>();
        private int nextId = 1;

        @Override
        public void save(Room room) {
            room.setId(nextId++);
            rooms.add(room);
        }

        @Override
        public void update(Room room) {
            rooms.removeIf(r -> r.getId() == room.getId());
            rooms.add(room);
        }

        @Override
        public void updateStatus(int id, RoomStatus status, Connection conn) {
            // delegas al método sin conexión o implementas la lógica del mock
            updateStatus(id, status);
        }

        @Override
        public Optional<Room> findById(int id) {
            return rooms.stream().filter(r -> r.getId() == id).findFirst();
        }

        @Override
        public Optional<Room> findByRoomNumber(String roomNumber) {
            return rooms.stream().filter(r -> r.getRoomNumber().equals(roomNumber)).findFirst();
        }

        @Override
        public java.util.List<Room> findAll() {
            return new java.util.ArrayList<>(rooms);
        }

        @Override
        public java.util.List<Room> findByType(RoomType type) {
            return rooms.stream().filter(r -> r.getType() == type).toList();
        }

        @Override
        public java.util.List<Room> findByStatus(RoomStatus status) {
            return rooms.stream().filter(r -> r.getStatus() == status).toList();
        }

        @Override
        public void updateStatus(int id, RoomStatus status) {
            findById(id).ifPresent(r -> r.setStatus(status));
        }

        @Override
        public void updateActiveStatus(int id, boolean active) {
            findById(id).ifPresent(r -> r.setActive(active));
        }
    }
}