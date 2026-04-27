package com.hotelnova.util;

import com.hotelnova.reservation.Reservation;
import com.hotelnova.room.Room;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Utility class for exporting data to CSV files.
 * Generates two reports required by the assignment:
 *   1. habitaciones_export.csv — full list of all rooms
 *   2. reservas_activas.csv — only active reservations (PENDING/CHECKED_IN)
 *
 * Uses BufferedWriter for efficient file writing.
 * Each export logs success/failure via AppLogger.
 */
public class CsvExporter {

    private CsvExporter() {
    }

    /**
     * Exports the complete list of rooms to habitaciones_export.csv.
     * Columns: ID, Room Number, Type, Capacity, Price Per Night, Status, Active
     * @param rooms list of rooms to export
     * @throws RuntimeException if file writing fails
     */
    public static void exportRooms(List<Room> rooms) {
        String fileName = "habitaciones_export.csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Write CSV header
            writer.write("ID,Room Number,Type,Capacity,Price Per Night,Status,Active");
            writer.newLine();

            // Write each room as a CSV row
            for (Room room : rooms) {
                String line = String.format("%d,%s,%s,%d,%.2f,%s,%s",
                        room.getId(),
                        room.getRoomNumber(),
                        room.getType(),
                        room.getCapacity(),
                        room.getPricePerNight(),
                        room.getStatus(),
                        room.isActive() ? "YES" : "NO"
                );
                writer.write(line);
                writer.newLine();
            }

            AppLogger.info("CSV exported successfully: " + fileName + " (" + rooms.size() + " rooms)");

        } catch (IOException e) {
            AppLogger.error("Error exporting rooms to CSV", e);
            throw new RuntimeException("Error al exportar habitaciones: " + e.getMessage(), e);
        }
    }

    /**
     * Exports active reservations to reservas_activas.csv.
     * Only includes reservations with status PENDING or CHECKED_IN.
     * Columns: ID, Guest ID, Room ID, Check-In, Check-Out, Status, Total Cost
     * @param reservations list of active reservations to export
     * @throws RuntimeException if file writing fails
     */
    public static void exportActiveReservations(List<Reservation> reservations) {
        String fileName = "reservas_activas.csv";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            // Write CSV header
            writer.write("ID,Guest ID,Room ID,Check-In,Check-Out,Status,Total Cost");
            writer.newLine();

            // Write each reservation as a CSV row
            for (Reservation res : reservations) {
                String line = String.format("%d,%d,%d,%s,%s,%s,%.2f",
                        res.getId(),
                        res.getGuestId(),
                        res.getRoomId(),
                        res.getCheckInDate(),
                        res.getCheckOutDate(),
                        res.getStatus(),
                        res.getTotalCost()
                );
                writer.write(line);
                writer.newLine();
            }

            AppLogger.info("CSV exported successfully: " + fileName + " (" + reservations.size() + " reservations)");

        } catch (IOException e) {
            AppLogger.error("Error exporting reservations to CSV", e);
            throw new RuntimeException("Error al exportar reservas: " + e.getMessage(), e);
        }
    }
}