package com.hotelnova.ui;

import com.hotelnova.guest.Guest;
import com.hotelnova.guest.GuestController;
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
 * Guest management view.
 * Allows registering, editing, and toggling guest active status.
 * Layout mirrors RoomView: table on left, form panel on right.
 */
public class GuestView {

    private final Stage stage;
    private final User currentUser;
    private final GuestController guestController;

    private TableView<Guest> table;
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField documentField;
    private TextField phoneField;
    private TextField emailField;
    private Label statusLabel;

    public GuestView(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.guestController = new GuestController();
    }

    public void show() {
        Label title = new Label("Gestión de Huéspedes");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        table = buildTable();
        loadGuests();

        VBox formPanel = buildFormPanel();

        Button showActiveBtn = new Button("Solo activos");
        showActiveBtn.setOnAction(e -> {
            List<Guest> active = guestController.findActiveGuests();
            table.setItems(FXCollections.observableArrayList(active));
        });

        Button reloadBtn = new Button(" Todos");
        reloadBtn.setOnAction(e -> loadGuests());

        HBox filterBar = new HBox(8, showActiveBtn, reloadBtn);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Volver al menú");
        backBtn.setOnAction(e -> new MainMenuView(stage, currentUser).show());

        HBox content = new HBox(16, table, formPanel);
        VBox root = new VBox(12, backBtn, title, filterBar, content);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #ecf0f1;");

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #ecf0f1;");

        Scene scene = new Scene(scrollPane, 900, 560);
        stage.setTitle("HotelNova - Huéspedes");
        stage.setScene(scene);
        stage.show();
    }

    @SuppressWarnings("unchecked")
    private TableView<Guest> buildTable() {
        TableView<Guest> tv = new TableView<>();
        tv.setPrefWidth(520);

        TableColumn<Guest, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(40);

        TableColumn<Guest, String> firstNameCol = new TableColumn<>("Nombre");
        firstNameCol.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameCol.setPrefWidth(100);

        TableColumn<Guest, String> lastNameCol = new TableColumn<>("Apellido");
        lastNameCol.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameCol.setPrefWidth(100);

        TableColumn<Guest, String> docCol = new TableColumn<>("Documento");
        docCol.setCellValueFactory(new PropertyValueFactory<>("documentNumber"));
        docCol.setPrefWidth(100);

        TableColumn<Guest, String> phoneCol = new TableColumn<>("Teléfono");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneCol.setPrefWidth(90);

        TableColumn<Guest, Boolean> activeCol = new TableColumn<>("Activo");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(60);

        tv.getColumns().addAll(idCol, firstNameCol, lastNameCol, docCol, phoneCol, activeCol);

        tv.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) populateForm(selected);
        });

        return tv;
    }

    private VBox buildFormPanel() {
        Label formTitle = new Label("Datos del huésped");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        firstNameField = new TextField();
        firstNameField.setPromptText("Nombre");

        lastNameField = new TextField();
        lastNameField.setPromptText("Apellido");

        documentField = new TextField();
        documentField.setPromptText("Número de documento");

        phoneField = new TextField();
        phoneField.setPromptText("Teléfono");

        emailField = new TextField();
        emailField.setPromptText("Correo electrónico");

        statusLabel = new Label();

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
                new Label("Nombre:"), firstNameField,
                new Label("Apellido:"), lastNameField,
                new Label("Documento:"), documentField,
                new Label("Teléfono:"), phoneField,
                new Label("Email:"), emailField,
                saveBtn, clearBtn,
                new Separator(),
                toggleBtn,
                statusLabel
        );
        panel.setPadding(new Insets(16));
        panel.setPrefWidth(300);
        panel.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        return panel;
    }

    private void loadGuests() {
        List<Guest> guests = guestController.findAll();
        table.setItems(FXCollections.observableArrayList(guests));
        HttpLogger.get("/api/guests", "Listed " + guests.size() + " guests");
    }

    private void handleSave() {
        try {
            String firstName = firstNameField.getText().trim();
            String lastName  = lastNameField.getText().trim();
            String document  = documentField.getText().trim();
            String phone     = phoneField.getText().trim();
            String email     = emailField.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || document.isEmpty()) {
                showStatus("Nombre, apellido y documento son obligatorios.", true);
                return;
            }

            Guest selected = table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                guestController.register(firstName, lastName, document, phone, email);
                HttpLogger.post("/api/guests", "Guest registered: " + firstName + " " + lastName);
                AppLogger.info("New guest registered: " + document);
                showStatus("Huésped registrado exitosamente.", false);
            } else {
                selected.setFirstName(firstName);
                selected.setLastName(lastName);
                selected.setDocumentNumber(document);
                selected.setPhone(phone);
                selected.setEmail(email);
                guestController.update(selected);
                HttpLogger.patch("/api/guests/" + selected.getId(), "Guest updated: " + document);
                showStatus("Huésped actualizado exitosamente.", false);
            }

            loadGuests();
            clearForm();

        } catch (RuntimeException e) {
            showStatus(e.getMessage(), true);
            AppLogger.error("Error saving guest", e);
        }
    }

    private void handleToggleActive() {
        Guest selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Seleccione un huésped de la tabla.", true);
            return;
        }
        boolean newStatus = !selected.isActive();
        guestController.toggleActiveStatus(selected.getId(), newStatus);
        HttpLogger.patch("/api/guests/" + selected.getId() + "/active",
                "Guest active=" + newStatus);
        loadGuests();
        showStatus("Estado cambiado correctamente.", false);
    }

    private void populateForm(Guest guest) {
        firstNameField.setText(guest.getFirstName());
        lastNameField.setText(guest.getLastName());
        documentField.setText(guest.getDocumentNumber());
        phoneField.setText(guest.getPhone() != null ? guest.getPhone() : "");
        emailField.setText(guest.getEmail() != null ? guest.getEmail() : "");
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        documentField.clear();
        phoneField.clear();
        emailField.clear();
        statusLabel.setText("");
        table.getSelectionModel().clearSelection();
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + (isError ? "red" : "green") + ";");
    }
}