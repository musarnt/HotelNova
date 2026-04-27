package com.hotelnova.ui;

import com.hotelnova.user.User;
import com.hotelnova.user.UserRole;
import com.hotelnova.util.AppLogger;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Main navigation menu shown after a successful login.
 * Displays module buttons based on the logged-in user's role:
 * - ADMIN: all modules including User management
 * - RECEPTIONIST: Rooms, Guests, Reservations, and Exports only
 */
public class MainMenuView {

    private final Stage stage;
    private final User currentUser;

    public MainMenuView(Stage stage, User currentUser) {
        this.stage = stage;
        this.currentUser = currentUser;
    }

    /**
     * Builds and displays the main menu.
     * Creates a button grid with available modules.
     * Admin sees all 5 modules; Receptionist sees 4.
     */
    public void show() {
        // --- Header ---
        Label titleLabel = new Label("HotelNova");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label welcomeLabel = new Label("Bienvenido, " + currentUser.getUsername() +
                " | Rol: " + currentUser.getRole());
        welcomeLabel.setFont(Font.font("Arial", 13));
        welcomeLabel.setStyle("-fx-text-fill: #7f8c8d;");

        // --- Module buttons ---
        Button roomsBtn       = createModuleButton("  Habitaciones",  "#2980b9");
        Button guestsBtn      = createModuleButton("  Huéspedes",     "#27ae60");
        Button reservationsBtn= createModuleButton("  Reservas",      "#8e44ad");
        Button exportsBtn     = createModuleButton("  Exportaciones", "#e67e22");
        Button usersBtn       = createModuleButton("  Usuarios",      "#c0392b");
        Button logoutBtn      = createModuleButton("  Cerrar sesión", "#7f8c8d");

        // --- Navigate to each module view ---
        roomsBtn.setOnAction(e -> new RoomView(stage, currentUser).show());
        guestsBtn.setOnAction(e -> new GuestView(stage, currentUser).show());
        reservationsBtn.setOnAction(e -> new ReservationView(stage, currentUser).show());
        exportsBtn.setOnAction(e -> new ExportView(stage, currentUser).show());
        logoutBtn.setOnAction(e -> handleLogout());

        // --- Users module: ADMIN only ---
        if (currentUser.getRole() == UserRole.ADMIN) {
            usersBtn.setOnAction(e -> new UserView(stage, currentUser).show());
        }

        // --- Build button grid ---
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(16);
        grid.setAlignment(Pos.CENTER);

        grid.add(roomsBtn,        0, 0);
        grid.add(guestsBtn,       1, 0);
        grid.add(reservationsBtn, 0, 1);
        grid.add(exportsBtn,      1, 1);

        // Only show Users button to ADMIN
        if (currentUser.getRole() == UserRole.ADMIN) {
            grid.add(usersBtn, 0, 2);
            grid.add(logoutBtn, 1, 2);
        } else {
            grid.add(logoutBtn, 0, 2);
        }

        // --- Header layout ---
        VBox header = new VBox(4, titleLabel, welcomeLabel, new Separator());
        header.setAlignment(Pos.CENTER_LEFT);

        // --- Root layout ---
        VBox root = new VBox(24, header, grid);
        root.setPadding(new Insets(32));
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #ecf0f1;");

        Scene scene = new Scene(root, 600, 420);
        stage.setTitle("HotelNova - Menú Principal");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Creates a styled module button with consistent size and color.
     * @param text button label
     * @param color hex background color
     * @return the styled Button
     */
    private Button createModuleButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(240, 70);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 8;");
        return btn;
    }

    /**
     * Handles logout — logs the event and returns to the login screen.
     */
    private void handleLogout() {
        AppLogger.info("User logged out: " + currentUser.getUsername());
        LoginView loginView = new LoginView(stage);
        loginView.show();
    }
}