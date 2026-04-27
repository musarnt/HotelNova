package com.hotelnova.ui;

import com.hotelnova.room.*;
import com.hotelnova.user.User;
import com.hotelnova.util.AppLogger;
import com.hotelnova.util.HttpLogger;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.List;

/**
 * Room management view.
 * Allows listing, registering, editing, and toggling room status.
 * Uses a TableView to display rooms and a form panel for data entry.
 */
public class RoomView {

    private final Stage stage;
    private final User currentUser;
    private final RoomController roomController;

    // Table and form fields kept as instance variables for cross-method access
    private TableView<Room> table;
    private TextField roomNumberField;
    private ComboBox<RoomType> typeCombo;
    private TextField capacityField;
    private TextField priceField;
    private Label statusLabel;

    public RoomView(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.roomController = new RoomController();
    }

    /**
     * Builds and displays the room management screen.
     * Layout: TableView on the left, form panel on the right.
     */
    public void show() {
        Label title = new Label("Gestión de Habitaciones");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        // --- Table ---
        table = buildTable();
        loadRooms();

        // --- Form panel ---
        VBox formPanel = buildFormPanel();

        // --- Filter bar ---
        HBox filterBar = buildFilterBar();

        // --- Back button ---
        Button backBtn = new Button("← Volver al menú");
        backBtn.setOnAction(e -> new MainMenuView(stage, currentUser).show());

        // --- Root layout ---
        HBox content = new HBox(16, table, formPanel);
        content.setPadding(new Insets(0));

        VBox root = new VBox(12, backBtn, title, filterBar, content);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #ecf0f1;");

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #ecf0f1;");

        Scene scene = new Scene(scrollPane, 900, 560);
        stage.setTitle("HotelNova - Habitaciones");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    /**
     * Builds the rooms TableView with columns for each field.
     */
    @SuppressWarnings("unchecked")
    private TableView<Room> buildTable() {
        TableView<Room> tv = new TableView<>();
        tv.setPrefWidth(520);

        TableColumn<Room, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(40);

        TableColumn<Room, String> numberCol = new TableColumn<>("Número");
        numberCol.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        numberCol.setPrefWidth(70);

        TableColumn<Room, RoomType> typeCol = new TableColumn<>("Tipo");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(80);

        TableColumn<Room, Integer> capacityCol = new TableColumn<>("Capacidad");
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        capacityCol.setPrefWidth(75);

        TableColumn<Room, Double> priceCol = new TableColumn<>("Precio/Noche");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("pricePerNight"));
        priceCol.setPrefWidth(95);

        TableColumn<Room, RoomStatus> statusCol = new TableColumn<>("Estado");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(90);

        TableColumn<Room, Boolean> activeCol = new TableColumn<>("Activa");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(55);

        tv.getColumns().addAll(idCol, numberCol, typeCol, capacityCol, priceCol, statusCol, activeCol);

        // When a row is selected, populate the form fields for editing
        tv.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) populateForm(selected);
        });

        return tv;
    }

    /**
     * Builds the right-side form panel for registering and editing rooms.
     */
    private VBox buildFormPanel() {
        Label formTitle = new Label("Datos de la habitación");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        roomNumberField = new TextField();
        roomNumberField.setPromptText("Ej: 101");

        typeCombo = new ComboBox<>(FXCollections.observableArrayList(RoomType.values()));
        typeCombo.setPromptText("Seleccione tipo");
        typeCombo.setMaxWidth(Double.MAX_VALUE);

        capacityField = new TextField();
        capacityField.setPromptText("Ej: 2");

        priceField = new TextField();
        priceField.setPromptText("Ej: 150000");

        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: green;");

        Button saveBtn = new Button(" Guardar");
        saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        saveBtn.setPrefWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> handleSave());

        Button clearBtn = new Button(" Limpiar");
        clearBtn.setPrefWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> clearForm());

        Button toggleBtn = new Button(" Activar / Desactivar");
        toggleBtn.setPrefWidth(Double.MAX_VALUE);
        toggleBtn.setOnAction(e -> handleToggleActive());

        VBox panel = new VBox(10,
                formTitle,
                new Label("Número:"), roomNumberField,
                new Label("Tipo:"), typeCombo,
                new Label("Capacidad:"), capacityField,
                new Label("Precio por noche:"), priceField,
                saveBtn,
                clearBtn,
                new Separator(),
                toggleBtn,
                statusLabel
        );
        panel.setPadding(new Insets(16));
        panel.setPrefWidth(300);
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 8;");

        return panel;
    }

    /**
     * Builds the filter bar above the table.
     * Allows filtering by type or status, and reloading all rooms.
     */
    private HBox buildFilterBar() {
        ComboBox<RoomType> typeFilter = new ComboBox<>(FXCollections.observableArrayList(RoomType.values()));
        typeFilter.setPromptText("Filtrar por tipo");

        ComboBox<RoomStatus> statusFilter = new ComboBox<>(FXCollections.observableArrayList(RoomStatus.values()));
        statusFilter.setPromptText("Filtrar por estado");

        Button filterTypeBtn = new Button("Filtrar tipo");
        filterTypeBtn.setOnAction(e -> {
            if (typeFilter.getValue() != null) {
                List<Room> filtered = roomController.findByType(typeFilter.getValue());
                table.setItems(FXCollections.observableArrayList(filtered));
                HttpLogger.get("/api/rooms?type=" + typeFilter.getValue(), "Filtered " + filtered.size() + " rooms");
            }
        });

        Button filterStatusBtn = new Button("Filtrar estado");
        filterStatusBtn.setOnAction(e -> {
            if (statusFilter.getValue() != null) {
                List<Room> filtered = roomController.findByStatus(statusFilter.getValue());
                table.setItems(FXCollections.observableArrayList(filtered));
                HttpLogger.get("/api/rooms?status=" + statusFilter.getValue(), "Filtered " + filtered.size() + " rooms");
            }
        });

        Button reloadBtn = new Button(" Todos");
        reloadBtn.setOnAction(e -> loadRooms());

        HBox bar = new HBox(8, typeFilter, filterTypeBtn, statusFilter, filterStatusBtn, reloadBtn);
        bar.setAlignment(Pos.CENTER_LEFT);
        return bar;
    }

    /**
     * Loads all rooms from the database into the table.
     */
    private void loadRooms() {
        List<Room> rooms = roomController.findAll();
        table.setItems(FXCollections.observableArrayList(rooms));
        HttpLogger.get("/api/rooms", "Listed " + rooms.size() + " rooms");
    }

    /**
     * Handles save — determines whether to register a new room or update an existing one.
     * New room: no row selected in table.
     * Update: a row is selected.
     */
    private void handleSave() {
        try {
            String number = roomNumberField.getText().trim();
            RoomType type = typeCombo.getValue();
            String capacityText = capacityField.getText().trim();
            String priceText = priceField.getText().trim();

            // Basic input validation
            if (number.isEmpty() || type == null || capacityText.isEmpty() || priceText.isEmpty()) {
                showStatus("Complete todos los campos.", true);
                return;
            }

            int capacity = Integer.parseInt(capacityText);
            double price = Double.parseDouble(priceText);

            Room selected = table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                // --- Register new room ---
                roomController.register(number, type, capacity, price);
                HttpLogger.post("/api/rooms", "Room " + number + " registered");
                AppLogger.info("New room registered: " + number);
                showStatus("Habitación registrada exitosamente.", false);
            } else {
                // --- Update existing room ---
                selected.setRoomNumber(number);
                selected.setType(type);
                selected.setCapacity(capacity);
                selected.setPricePerNight(price);
                roomController.update(selected);
                HttpLogger.patch("/api/rooms/" + selected.getId(), "Room " + number + " updated");
                AppLogger.info("Room updated: " + number);
                showStatus("Habitación actualizada exitosamente.", false);
            }

            loadRooms();
            clearForm();

        } catch (NumberFormatException e) {
            showStatus("Capacidad y precio deben ser números.", true);
        } catch (RuntimeException e) {
            showStatus(e.getMessage(), true);
            AppLogger.error("Error saving room", e);
        }
    }

    /**
     * Toggles the active status of the selected room.
     */
    private void handleToggleActive() {
        Room selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Seleccione una habitación de la tabla.", true);
            return;
        }

        boolean newStatus = !selected.isActive();
        roomController.toggleActiveStatus(selected.getId(), newStatus);
        HttpLogger.patch("/api/rooms/" + selected.getId() + "/active",
                "Room " + selected.getRoomNumber() + " active=" + newStatus);
        loadRooms();
        showStatus("Estado cambiado correctamente.", false);
    }

    /**
     * Fills the form fields with data from the selected room row.
     */
    private void populateForm(Room room) {
        roomNumberField.setText(room.getRoomNumber());
        typeCombo.setValue(room.getType());
        capacityField.setText(String.valueOf(room.getCapacity()));
        priceField.setText(String.valueOf(room.getPricePerNight()));
    }

    /**
     * Clears all form fields and deselects the table row.
     */
    private void clearForm() {
        roomNumberField.clear();
        typeCombo.setValue(null);
        capacityField.clear();
        priceField.clear();
        statusLabel.setText("");
        table.getSelectionModel().clearSelection();
    }

    /**
     * Displays a status message in green (success) or red (error).
     */
    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + (isError ? "red" : "green") + ";");
    }
}