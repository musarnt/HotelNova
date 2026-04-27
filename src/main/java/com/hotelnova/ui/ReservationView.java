package com.hotelnova.ui;

import com.hotelnova.reservation.Reservation;
import com.hotelnova.reservation.ReservationController;
import com.hotelnova.user.User;
import com.hotelnova.util.AppLogger;
import com.hotelnova.util.HttpLogger;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

/**
 * Reservation management view.
 * Handles creating reservations, check-in, and check-out.
 * All business validations are done in the service layer — this view
 * only captures input and shows the result to the user.
 */
public class ReservationView {

    private final Stage stage;
    private final User currentUser;
    private final ReservationController reservationController;

    private TableView<Reservation> table;
    private TextField guestIdField;
    private TextField roomIdField;
    private DatePicker checkInPicker;
    private DatePicker checkOutPicker;
    private Label statusLabel;
    private Label costPreviewLabel;

    public ReservationView(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.reservationController = new ReservationController();
    }

    public void show() {
        Label title = new Label("Gestión de Reservas");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        table = buildTable();
        loadReservations();

        VBox formPanel = buildFormPanel();

        Button showActiveBtn = new Button("Solo activas");
        showActiveBtn.setOnAction(e -> {
            List<Reservation> active = reservationController.findActiveReservations();
            table.setItems(FXCollections.observableArrayList(active));
        });

        Button reloadBtn = new Button(" Todas");
        reloadBtn.setOnAction(e -> loadReservations());

        HBox filterBar = new HBox(8, showActiveBtn, reloadBtn);

        Button backBtn = new Button("← Volver al menú");
        backBtn.setOnAction(e -> new MainMenuView(stage, currentUser).show());

        HBox content = new HBox(16, table, formPanel);
        VBox root = new VBox(12, backBtn, title, filterBar, content);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #ecf0f1;");

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #ecf0f1;");

        Scene scene = new Scene(scrollPane, 1000, 580);
        stage.setTitle("HotelNova - Reservas");
        stage.setScene(scene);
        stage.show();
    }

    @SuppressWarnings("unchecked")
    private TableView<Reservation> buildTable() {
        TableView<Reservation> tv = new TableView<>();
        tv.setPrefWidth(580);

        TableColumn<Reservation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(40);

        TableColumn<Reservation, Integer> guestCol = new TableColumn<>("Huésped ID");
        guestCol.setCellValueFactory(new PropertyValueFactory<>("guestId"));
        guestCol.setPrefWidth(85);

        TableColumn<Reservation, Integer> roomCol = new TableColumn<>("Hab. ID");
        roomCol.setCellValueFactory(new PropertyValueFactory<>("roomId"));
        roomCol.setPrefWidth(65);

        TableColumn<Reservation, LocalDate> checkInCol = new TableColumn<>("Check-In");
        checkInCol.setCellValueFactory(new PropertyValueFactory<>("checkInDate"));
        checkInCol.setPrefWidth(90);

        TableColumn<Reservation, LocalDate> checkOutCol = new TableColumn<>("Check-Out");
        checkOutCol.setCellValueFactory(new PropertyValueFactory<>("checkOutDate"));
        checkOutCol.setPrefWidth(90);

        TableColumn<Reservation, String> statusCol = new TableColumn<>("Estado");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(95);

        TableColumn<Reservation, Double> costCol = new TableColumn<>("Costo Total");
        costCol.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        costCol.setPrefWidth(95);

        tv.getColumns().addAll(idCol, guestCol, roomCol, checkInCol, checkOutCol, statusCol, costCol);

        // When a reservation is selected, show estimated cost preview
        tv.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) populateForm(selected);
        });

        return tv;
    }

    private VBox buildFormPanel() {
        Label formTitle = new Label("Nueva reserva");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        guestIdField = new TextField();
        guestIdField.setPromptText("ID del huésped");

        roomIdField = new TextField();
        roomIdField.setPromptText("ID de la habitación");

        checkInPicker = new DatePicker();
        checkInPicker.setPromptText("Fecha de llegada");
        checkInPicker.setMaxWidth(Double.MAX_VALUE);

        checkOutPicker = new DatePicker();
        checkOutPicker.setPromptText("Fecha de salida");
        checkOutPicker.setMaxWidth(Double.MAX_VALUE);

        costPreviewLabel = new Label("Costo estimado: $0.00");
        costPreviewLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9;");

        statusLabel = new Label();

        // Update cost preview whenever dates or room change
        checkOutPicker.setOnAction(e -> updateCostPreview());

        Button createBtn = new Button("📋 Crear Reserva");
        createBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");
        createBtn.setPrefWidth(Double.MAX_VALUE);
        createBtn.setOnAction(e -> handleCreate());

        // Check-in and check-out as separate action buttons
        Button checkInBtn = new Button(" Hacer Check-In");
        checkInBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        checkInBtn.setPrefWidth(Double.MAX_VALUE);
        checkInBtn.setOnAction(e -> handleCheckIn());

        Button checkOutBtn = new Button(" Hacer Check-Out");
        checkOutBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white;");
        checkOutBtn.setPrefWidth(Double.MAX_VALUE);
        checkOutBtn.setOnAction(e -> handleCheckOut());

        Button clearBtn = new Button(" Limpiar");
        clearBtn.setPrefWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> clearForm());

        VBox panel = new VBox(10,
                formTitle,
                new Label("ID Huésped:"), guestIdField,
                new Label("ID Habitación:"), roomIdField,
                new Label("Check-In:"), checkInPicker,
                new Label("Check-Out:"), checkOutPicker,
                costPreviewLabel,
                createBtn,
                new Separator(),
                new Label("Reserva seleccionada:"),
                checkInBtn,
                checkOutBtn,
                clearBtn,
                statusLabel
        );
        panel.setPadding(new Insets(16));
        panel.setPrefWidth(320);
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        return panel;
    }

    private void loadReservations() {
        List<Reservation> reservations = reservationController.findAll();
        table.setItems(FXCollections.observableArrayList(reservations));
        HttpLogger.get("/api/reservations", "Listed " + reservations.size() + " reservations");
    }

    /**
     * Creates a new reservation with the form data.
     * All validations (guest active, room available, dates, overlap) run in the service.
     */
    private void handleCreate() {
        try {
            int guestId = Integer.parseInt(guestIdField.getText().trim());
            int roomId  = Integer.parseInt(roomIdField.getText().trim());
            LocalDate checkIn  = checkInPicker.getValue();
            LocalDate checkOut = checkOutPicker.getValue();

            if (checkIn == null || checkOut == null) {
                showStatus("Seleccione las fechas de check-in y check-out.", true);
                return;
            }

            reservationController.createReservation(guestId, roomId, checkIn, checkOut);
            HttpLogger.post("/api/reservations", "Reservation created for guest=" + guestId + " room=" + roomId);
            AppLogger.info("Reservation created: guest=" + guestId + " room=" + roomId);
            showStatus("Reserva creada exitosamente.", false);
            loadReservations();
            clearForm();

        } catch (NumberFormatException e) {
            showStatus("Los IDs de huésped y habitación deben ser números.", true);
        } catch (RuntimeException e) {
            showStatus(e.getMessage(), true);
            AppLogger.error("Error creating reservation", e);
        }
    }

    /**
     * Processes check-in for the selected reservation.
     * Uses a JDBC transaction in the service: updates reservation + room status atomically.
     */
    private void handleCheckIn() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Seleccione una reserva de la tabla.", true);
            return;
        }

        try {
            reservationController.checkIn(selected.getId());
            HttpLogger.patch("/api/reservations/" + selected.getId() + "/checkin",
                    "Check-in processed for reservation " + selected.getId());
            AppLogger.info("Check-in: reservation " + selected.getId());
            showStatus("Check-in realizado exitosamente.", false);
            loadReservations();
        } catch (RuntimeException e) {
            showStatus(e.getMessage(), true);
            AppLogger.error("Error during check-in", e);
        }
    }

    /**
     * Processes check-out for the selected reservation.
     * Calculates final cost (nights × price × IVA) and frees the room.
     */
    private void handleCheckOut() {
        Reservation selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Seleccione una reserva de la tabla.", true);
            return;
        }

        try {
            reservationController.checkOut(selected.getId());
            HttpLogger.patch("/api/reservations/" + selected.getId() + "/checkout",
                    "Check-out processed for reservation " + selected.getId());
            AppLogger.info("Check-out: reservation " + selected.getId());
            showStatus("Check-out realizado. Costo total calculado.", false);
            loadReservations();
        } catch (RuntimeException e) {
            showStatus(e.getMessage(), true);
            AppLogger.error("Error during check-out", e);
        }
    }

    /**
     * Updates the cost preview label when dates are selected.
     * Shows estimated cost before confirming the reservation.
     */
    private void updateCostPreview() {
        try {
            LocalDate checkIn  = checkInPicker.getValue();
            LocalDate checkOut = checkOutPicker.getValue();
            String roomIdText  = roomIdField.getText().trim();

            if (checkIn != null && checkOut != null && !roomIdText.isEmpty()) {
                long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
                if (nights > 0) {
                    // Use a default price for preview — actual price comes from the room
                    costPreviewLabel.setText("Noches: " + nights + " | Costo se calculará al confirmar");
                }
            }
        } catch (Exception ignored) {
            // Silently ignore preview errors — not critical
        }
    }

    private void populateForm(Reservation reservation) {
        guestIdField.setText(String.valueOf(reservation.getGuestId()));
        roomIdField.setText(String.valueOf(reservation.getRoomId()));
        checkInPicker.setValue(reservation.getCheckInDate());
        checkOutPicker.setValue(reservation.getCheckOutDate());
    }

    private void clearForm() {
        guestIdField.clear();
        roomIdField.clear();
        checkInPicker.setValue(null);
        checkOutPicker.setValue(null);
        costPreviewLabel.setText("Costo estimado: $0.00");
        statusLabel.setText("");
        table.getSelectionModel().clearSelection();
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + (isError ? "red" : "green") + ";");
    }
}