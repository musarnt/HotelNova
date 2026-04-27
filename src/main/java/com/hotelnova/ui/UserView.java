package com.hotelnova.ui;

import com.hotelnova.user.*;
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

import java.util.List;

/**
 * User management view — accessible only to ADMIN role.
 * Allows registering new users, updating them, and toggling active status.
 * Passwords are never shown in the table (security).
 */
public class UserView {

    private final Stage stage;
    private final User currentUser;
    private final UserController userController;

    private TableView<User> table;
    private TextField usernameField;
    private PasswordField passwordField;
    private ComboBox<UserRole> roleCombo;
    private Label statusLabel;

    public UserView(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
        this.userController = new UserController();
    }

    public void show() {
        Label title = new Label("Gestión de Usuarios");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        table = buildTable();
        loadUsers();

        VBox formPanel = buildFormPanel();

        Button backBtn = new Button("← Volver al menú");
        backBtn.setOnAction(e -> new MainMenuView(stage, currentUser).show());

        HBox content = new HBox(16, table, formPanel);
        VBox root = new VBox(12, backBtn, title, content);
        root.setPadding(new Insets(24));
        root.setStyle("-fx-background-color: #ecf0f1;");

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #ecf0f1;");

        Scene scene = new Scene(scrollPane, 860, 500);
        stage.setTitle("HotelNova - Usuarios");
        stage.setScene(scene);
        stage.show();
    }

    @SuppressWarnings("unchecked")
    private TableView<User> buildTable() {
        TableView<User> tv = new TableView<>();
        tv.setPrefWidth(500);

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(40);

        TableColumn<User, String> usernameCol = new TableColumn<>("Usuario");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(140);

        TableColumn<User, UserRole> roleCol = new TableColumn<>("Rol");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(120);

        TableColumn<User, Boolean> activeCol = new TableColumn<>("Activo");
        activeCol.setCellValueFactory(new PropertyValueFactory<>("active"));
        activeCol.setPrefWidth(60);

        // Password column intentionally omitted — never display passwords
        tv.getColumns().addAll(idCol, usernameCol, roleCol, activeCol);

        tv.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected != null) {
                usernameField.setText(selected.getUsername());
                roleCombo.setValue(selected.getRole());
                // Leave password blank — user must re-enter if they want to change it
                passwordField.clear();
            }
        });

        return tv;
    }

    private VBox buildFormPanel() {
        Label formTitle = new Label("Datos del usuario");
        formTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        usernameField = new TextField();
        usernameField.setPromptText("Nombre de usuario");

        passwordField = new PasswordField();
        passwordField.setPromptText("Contraseña (dejar en blanco para no cambiar)");

        roleCombo = new ComboBox<>(FXCollections.observableArrayList(UserRole.values()));
        roleCombo.setPromptText("Seleccione rol");
        roleCombo.setMaxWidth(Double.MAX_VALUE);

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

        Label hint = new Label("* La contraseña se almacena con BCrypt hash");
        hint.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");

        VBox panel = new VBox(10,
                formTitle,
                new Label("Usuario:"), usernameField,
                new Label("Contraseña:"), passwordField,
                hint,
                new Label("Rol:"), roleCombo,
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

    private void loadUsers() {
        List<User> users = userController.findAll();
        table.setItems(FXCollections.observableArrayList(users));
        HttpLogger.get("/api/users", "Listed " + users.size() + " users");
    }

    private void handleSave() {
        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            UserRole role   = roleCombo.getValue();

            if (username.isEmpty() || role == null) {
                showStatus("Usuario y rol son obligatorios.", true);
                return;
            }

            User selected = table.getSelectionModel().getSelectedItem();

            if (selected == null) {
                // New user — password required
                if (password.isEmpty()) {
                    showStatus("La contraseña es obligatoria al registrar.", true);
                    return;
                }
                userController.register(username, password, role);
                HttpLogger.post("/api/users", "User registered: " + username);
                AppLogger.info("New user registered: " + username);
                showStatus("Usuario registrado exitosamente.", false);
            } else {
                selected.setUsername(username);
                selected.setRole(role);
                // Only update password if a new one was entered
                if (!password.isEmpty()) {
                    selected.setPassword(password);
                }
                userController.update(selected);
                HttpLogger.patch("/api/users/" + selected.getId(), "User updated: " + username);
                showStatus("Usuario actualizado exitosamente.", false);
            }

            loadUsers();
            clearForm();

        } catch (RuntimeException e) {
            showStatus(e.getMessage(), true);
            AppLogger.error("Error saving user", e);
        }
    }

    private void handleToggleActive() {
        User selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("Seleccione un usuario de la tabla.", true);
            return;
        }
        boolean newStatus = !selected.isActive();
        userController.toggleActiveStatus(selected.getId(), newStatus);
        HttpLogger.patch("/api/users/" + selected.getId() + "/active", "User active=" + newStatus);
        loadUsers();
        showStatus("Estado cambiado correctamente.", false);
    }

    private void clearForm() {
        usernameField.clear();
        passwordField.clear();
        roleCombo.setValue(null);
        statusLabel.setText("");
        table.getSelectionModel().clearSelection();
    }

    private void showStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-text-fill: " + (isError ? "red" : "green") + ";");
    }
}