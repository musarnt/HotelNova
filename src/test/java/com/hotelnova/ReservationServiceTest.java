package com.hotelnova;

import com.hotelnova.exception.*;
import com.hotelnova.guest.*;
import com.hotelnova.reservation.*;
import com.hotelnova.room.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReservationService business logic.
 * Covers all 7 validations required by the assignment.
 * Uses mock DAOs to run without a real database connection.
 */
class ReservationServiceTest {

    private ReservationService reservationService;
    private MockReservationDao reservationDao;
    private MockRoomDao roomDao;
    private MockGuestDao guestDao;

    // Reusable test data
    private Room availableRoom;
    private Room occupiedRoom;
    private Guest activeGuest;
    private Guest inactiveGuest;

    @BeforeEach
    void setUp() {
        reservationDao = new MockReservationDao();
        roomDao        = new MockRoomDao();
        guestDao       = new MockGuestDao();

        reservationService = new ReservationService(reservationDao, roomDao, guestDao);

        // Set up an available room
        availableRoom = new Room(1, "101", RoomType.SINGLE, 1, 100000,
                RoomStatus.AVAILABLE, true, LocalDateTime.now());

        // Set up an occupied room
        occupiedRoom = new Room(2, "102", RoomType.DOUBLE, 2, 150000,
                RoomStatus.OCCUPIED, true, LocalDateTime.now());

        // Set up an active guest
        activeGuest = new Guest(1, "Juan", "Pérez", "123456789",
                "300000000", "juan@test.com", true, LocalDateTime.now());

        // Set up an inactive guest
        inactiveGuest = new Guest(2, "María", "López", "987654321",
                "311000000", "maria@test.com", false, LocalDateTime.now());

        roomDao.rooms.add(availableRoom);
        roomDao.rooms.add(occupiedRoom);
        guestDao.guests.add(activeGuest);
        guestDao.guests.add(inactiveGuest);
    }

    // -------------------------------------------------------------------------
    // Test 1: Inactive guest cannot make a reservation
    // -------------------------------------------------------------------------

    /**
     * Business rule: only active guests can make reservations.
     * Attempting to reserve for an inactive guest must throw InactiveGuestException.
     */
    @Test
    void createReservation_inactiveGuest_throwsInactiveGuestException() {
        Reservation reservation = new Reservation(
                inactiveGuest.getId(), availableRoom.getId(),
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)
        );

        assertThrows(InactiveGuestException.class,
                () -> reservationService.createReservation(reservation));
    }

    // -------------------------------------------------------------------------
    // Test 2: Unavailable room cannot be reserved
    // -------------------------------------------------------------------------

    /**
     * Business rule: a room must be AVAILABLE to accept a reservation.
     * Attempting to reserve an OCCUPIED room must throw RoomNotAvailableException.
     */
    @Test
    void createReservation_occupiedRoom_throwsRoomNotAvailableException() {
        Reservation reservation = new Reservation(
                activeGuest.getId(), occupiedRoom.getId(),
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3)
        );

        assertThrows(RoomNotAvailableException.class,
                () -> reservationService.createReservation(reservation));
    }

    // -------------------------------------------------------------------------
    // Test 3: Check-in date must be before check-out date
    // -------------------------------------------------------------------------

    /**
     * Business rule: check-in must be strictly earlier than check-out.
     * Equal or reversed dates must throw InvalidDateRangeException.
     */
    @Test
    void createReservation_checkInAfterCheckOut_throwsInvalidDateRangeException() {
        LocalDate checkIn  = LocalDate.now().plusDays(5);
        LocalDate checkOut = LocalDate.now().plusDays(2); // checkOut before checkIn

        Reservation reservation = new Reservation(
                activeGuest.getId(), availableRoom.getId(), checkIn, checkOut
        );

        assertThrows(InvalidDateRangeException.class,
                () -> reservationService.createReservation(reservation));
    }

    /**
     * Edge case: same check-in and check-out date must also be rejected.
     */
    @Test
    void createReservation_sameCheckInAndCheckOut_throwsInvalidDateRangeException() {
        LocalDate sameDate = LocalDate.now().plusDays(3);

        Reservation reservation = new Reservation(
                activeGuest.getId(), availableRoom.getId(), sameDate, sameDate
        );

        assertThrows(InvalidDateRangeException.class,
                () -> reservationService.createReservation(reservation));
    }

    // -------------------------------------------------------------------------
    // Test 4: No overlapping reservations for the same room
    // -------------------------------------------------------------------------

    /**
     * Business rule: a room cannot have two reservations with overlapping dates.
     * Overlap logic: A.start < B.end AND B.start < A.end
     */
    @Test
    void createReservation_overlappingDates_throwsOverlappingReservationException() {
        // First reservation: days 5 to 10
        Reservation first = new Reservation(
                activeGuest.getId(), availableRoom.getId(),
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(10)
        );
        reservationService.createReservation(first);

        // Second reservation: days 7 to 12 — overlaps with first
        Reservation overlapping = new Reservation(
                activeGuest.getId(), availableRoom.getId(),
                LocalDate.now().plusDays(7), LocalDate.now().plusDays(12)
        );

        assertThrows(OverlappingReservationException.class,
                () -> reservationService.createReservation(overlapping));
    }

    // -------------------------------------------------------------------------
    // Test 5: Valid reservation is created successfully
    // -------------------------------------------------------------------------

    /**
     * Happy path: a reservation with valid data should be created without errors.
     */
    @Test
    void createReservation_validData_succeeds() {
        Reservation reservation = new Reservation(
                activeGuest.getId(), availableRoom.getId(),
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(4)
        );

        assertDoesNotThrow(() -> reservationService.createReservation(reservation));
    }

    // -------------------------------------------------------------------------
    // Test 6: Check-out without check-in must be rejected
    // -------------------------------------------------------------------------

    /**
     * Business rule: cannot check out a reservation that was never checked in.
     * Only CHECKED_IN reservations can be checked out.
     */
    @Test
    void checkOut_withoutCheckIn_throwsInvalidCheckoutException() {
        // Create a reservation in PENDING status (no check-in done)
        Reservation pending = new Reservation(1,
                activeGuest.getId(), availableRoom.getId(),
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                ReservationStatus.PENDING, 0, LocalDateTime.now()
        );
        reservationDao.reservations.add(pending);

        // Attempting check-out without check-in must throw
        assertThrows(InvalidCheckoutException.class,
                () -> reservationService.checkOut(pending.getId()));
    }

    // -------------------------------------------------------------------------
    // Test 7: Stay cost calculation
    // -------------------------------------------------------------------------

    /**
     * Business rule: total cost = nights × pricePerNight × (1 + IVA).
     * With IVA = 0.19 (19%), 3 nights at $100,000/night:
     * subtotal = 3 × 100,000 = 300,000
     * total    = 300,000 × 1.19 = 357,000
     */
    @Test
    void calculateStayCost_correctFormula() {
        double pricePerNight = 100_000;
        long nights = 3;
        double iva = 0.19;

        double expected = (nights * pricePerNight) * (1 + iva); // 357,000
        double result = reservationService.calculateStayCost(pricePerNight, nights);

        assertEquals(expected, result, 0.01,
                "Cost must be nights × price × (1 + IVA)");
    }

    // =========================================================================
    // Mock DAOs — in-memory implementations for testing without a database
    // =========================================================================

    static class MockReservationDao implements ReservationDao {
        final List<Reservation> reservations = new ArrayList<>();
        private int nextId = 1;

        @Override
        public void save(Reservation r) {
            r.setId(nextId++);
            reservations.add(r);
        }

        @Override
        public void update(Reservation r) {
            reservations.removeIf(x -> x.getId() == r.getId());
            reservations.add(r);
        }

        @Override
        public Optional<Reservation> findById(int id) {
            return reservations.stream().filter(r -> r.getId() == id).findFirst();
        }

        @Override
        public List<Reservation> findAll() {
            return new ArrayList<>(reservations);
        }

        @Override
        public List<Reservation> findActiveReservations() {
            return reservations.stream()
                    .filter(r -> r.getStatus() == ReservationStatus.PENDING
                            || r.getStatus() == ReservationStatus.CHECKED_IN)
                    .toList();
        }

        @Override
        public List<Reservation> findByGuestId(int guestId) {
            return reservations.stream().filter(r -> r.getGuestId() == guestId).toList();
        }

        @Override
        public List<Reservation> findByRoomId(int roomId) {
            return reservations.stream().filter(r -> r.getRoomId() == roomId).toList();
        }

        @Override
        public boolean hasOverlappingReservation(int roomId, LocalDate checkIn,
                                                 LocalDate checkOut, int excludeId) {
            // Range overlap: A.start < B.end AND B.start < A.end
            return reservations.stream()
                    .filter(r -> r.getRoomId() == roomId && r.getId() != excludeId)
                    .filter(r -> r.getStatus() == ReservationStatus.PENDING
                            || r.getStatus() == ReservationStatus.CHECKED_IN)
                    .anyMatch(r -> checkIn.isBefore(r.getCheckOutDate())
                            && r.getCheckInDate().isBefore(checkOut));
        }

        @Override
        public Optional<Reservation> findActiveReservationByRoomId(int roomId) {
            return reservations.stream()
                    .filter(r -> r.getRoomId() == roomId
                            && r.getStatus() == ReservationStatus.CHECKED_IN)
                    .findFirst();
        }
    }

    static class MockRoomDao implements RoomDao {
        final List<Room> rooms = new ArrayList<>();

        @Override
        public void save(Room r) { rooms.add(r); }

        @Override
        public void update(Room r) {
            rooms.removeIf(x -> x.getId() == r.getId());
            rooms.add(r);
        }

        @Override
        public Optional<Room> findById(int id) {
            return rooms.stream().filter(r -> r.getId() == id).findFirst();
        }

        @Override
        public Optional<Room> findByRoomNumber(String n) {
            return rooms.stream().filter(r -> r.getRoomNumber().equals(n)).findFirst();
        }

        @Override
        public List<Room> findAll() { return new ArrayList<>(rooms); }

        @Override
        public List<Room> findByType(RoomType t) {
            return rooms.stream().filter(r -> r.getType() == t).toList();
        }

        @Override
        public List<Room> findByStatus(RoomStatus s) {
            return rooms.stream().filter(r -> r.getStatus() == s).toList();
        }

        @Override
        public void updateStatus(int id, RoomStatus s) {
            findById(id).ifPresent(r -> r.setStatus(s));
        }

        @Override
        public void updateActiveStatus(int id, boolean a) {
            findById(id).ifPresent(r -> r.setActive(a));
        }
    }

    static class MockGuestDao implements GuestDao {
        final List<Guest> guests = new ArrayList<>();

        @Override
        public void save(Guest g) { guests.add(g); }

        @Override
        public void update(Guest g) {
            guests.removeIf(x -> x.getId() == g.getId());
            guests.add(g);
        }

        @Override
        public Optional<Guest> findById(int id) {
            return guests.stream().filter(g -> g.getId() == id).findFirst();
        }

        @Override
        public Optional<Guest> findByDocumentNumber(String doc) {
            return guests.stream().filter(g -> g.getDocumentNumber().equals(doc)).findFirst();
        }

        @Override
        public List<Guest> findAll() { return new ArrayList<>(guests); }

        @Override
        public List<Guest> findActiveGuests() {
            return guests.stream().filter(Guest::isActive).toList();
        }

        @Override
        public void updateActiveStatus(int id, boolean active) {
            findById(id).ifPresent(g -> g.setActive(active));
        }
    }
}