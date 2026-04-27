package com.hotelnova.ui;

import com.hotelnova.reservation.ReservationController;
import com.hotelnova.room.RoomController;
import com.hotelnova.user.User;
import com.hotelnova.util.AppLogger;
import com.hotelnova.util.CsvExporter;
import com.hotelnova.util.HttpLogger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Export view — allows generating the two required CSV reports:
 * 1. habitaciones_export.csv - full room list
 * 2. reservas_activas.csv - active reservations only
 *
 * Files are saved in the project root directory.
 */
public class ExportView {

    private final Stage stage;
    private final User currentUser;
    private final RoomController roomController;
    private final ReservationController reservationController;

    public ExportView(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.roomController = new RoomController();
        this.reservationController = new ReservationController();
    }

    public void show() {
        Label title = new Label("Exportaciones CSV");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Label infoLabel = new Label("Los archivos se generan en la carpeta raíz del proyecto.");
        infoLabel.setStyle("-fx-text-fill: #7f8c8d;");

        // --- Export rooms card ---
        Label roomsTitle = new Label("Habitaciones");
        roomsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label roomsDesc = new Label("Exporta el listado completo de habitaciones\na habitaciones_export.csv");
        Label roomsStatus = new Label();

        Button exportRoomsBtn = new Button(" Exportar habitaciones");
        exportRoomsBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        exportRoomsBtn.setPrefWidth(240);
        exportRoomsBtn.setOnAction(e -> handleExportRooms(roomsStatus));

        VBox roomsCard = buildCard(roomsTitle, roomsDesc, exportRoomsBtn, roomsStatus);

        // --- Export reservations card ---
        Label reservationsTitle = new Label("Reservas activas");
        reservationsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label reservationsDesc = new Label("Exporta solo reservas con estado PENDING o\nCHECKED_IN a reservas_activas.csv");
        Label reservationsStatus = new Label();

        Button exportReservationsBtn = new Button(" Exportar reservas activas");
        exportReservationsBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");
        exportReservationsBtn.setPrefWidth(240);
        exportReservationsBtn.setOnAction(e -> handleExportReservations(reservationsStatus));

        VBox reservationsCard = buildCard(reservationsTitle, reservationsDesc, exportReservationsBtn, reservationsStatus);

        // --- Back button ---
        Button backBtn = new Button("← Volver al menú");
        backBtn.setOnAction(e -> new MainMenuView(stage, currentUser).show());

        HBox cards = new HBox(24, roomsCard, reservationsCard);
        cards.setAlignment(Pos.CENTER);

        VBox root = new VBox(16, backBtn, title, infoLabel, cards);
        root.setPadding(new Insets(32));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #ecf0f1;");

        Scene scene = new Scene(root, 680, 380);
        stage.setTitle("HotelNova - Exportaciones");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Exports all rooms to CSV.
     * Uses CsvExporter utility and logs the operation.
     */
    private void handleExportRooms(Label statusLabel) {
        try {
            CsvExporter.exportRooms(roomController.findAll());
            HttpLogger.get("/api/rooms/export", "Rooms exported to habitaciones_export.csv");
            AppLogger.info("Rooms exported to CSV by user: " + currentUser.getUsername());
            statusLabel.setText(" Exportado: habitaciones_export.csv");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (RuntimeException e) {
            statusLabel.setText(" Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            AppLogger.error("Error exporting rooms to CSV", e);
        }
    }

    /**
     * Exports active reservations to CSV.
     * Only includes PENDING and CHECKED_IN reservations.
     */
    private void handleExportReservations(Label statusLabel) {
        try {
            CsvExporter.exportActiveReservations(reservationController.findActiveReservations());
            HttpLogger.get("/api/reservations/export", "Active reservations exported to reservas_activas.csv");
            AppLogger.info("Reservations exported to CSV by user: " + currentUser.getUsername());
            statusLabel.setText(" Exportado: reservas_activas.csv");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (RuntimeException e) {
            statusLabel.setText(" Error: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            AppLogger.error("Error exporting reservations to CSV", e);
        }
    }

    /**
     * Builds a styled card for each export option.
     */
    private VBox buildCard(Label title, Label description, Button button, Label status) {
        VBox card = new VBox(12, title, description, button, status);
        card.setPadding(new Insets(24));
        card.setPrefWidth(270);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        return card;
    }
}