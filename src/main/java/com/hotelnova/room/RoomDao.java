package com.hotelnova.room;

import java.util.List;
import java.util.Optional;
import java.sql.Connection;
import java.sql.SQLException;

public interface RoomDao {

    void save(Room room);

    void update(Room room);

    Optional<Room> findById(int id);

    Optional<Room> findByRoomNumber(String roomNumber);

    List<Room> findAll();

    List<Room> findByType(RoomType type);

    List<Room> findByStatus(RoomStatus status);

    void updateStatus(int id, RoomStatus status);
    void updateStatus(int id, RoomStatus status, Connection conn) throws SQLException;

    void updateActiveStatus(int id, boolean active);
}