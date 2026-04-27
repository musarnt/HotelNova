package com.hotelnova.ui;

import com.hotelnova.user.User;
import com.hotelnova.user.UserController;
import com.hotelnova.util.AppLogger;
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
 * Login screen — the first view the user sees.
 * Collects username and password, calls UserController.login(),
 * and navigates to MainMenuView on success.
 * Errors are displayed inline without closing the window.
 */
public class LoginView {

    private final Stage stage;
    private final UserController userController;

    public LoginView(Stage stage) {
        this.stage = stage;
        this.userController = new UserController();
    }

    /**
     * Builds and displays the login screen.
     * Layout: centered card with logo, username field, password field, and login button.
     */
    public void show() {
        // --- Title ---
        Label titleLabel = new Label("HotelNova");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        Label subtitleLabel = new Label("Sistema de Gestión de Hospedaje");
        subtitleLabel.setFont(Font.font("Arial", 14));

        // --- Form fields ---
        Label usernameLabel = new Label("Usuario:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Ingrese su usuario");
        usernameField.setMaxWidth(280);

        Label passwordLabel = new Label("Contraseña:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Ingrese su contraseña");
        passwordField.setMaxWidth(280);

        // --- Error label (hidden until login fails) ---
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(false);

        // --- Login button ---
        Button loginButton = new Button("Iniciar sesión");
        loginButton.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 14px;");
        loginButton.setMaxWidth(280);
        loginButton.setPrefHeight(40);

        // Allow pressing Enter from password field to trigger login
        passwordField.setOnAction(e -> handleLogin(usernameField, passwordField, errorLabel));
        loginButton.setOnAction(e -> handleLogin(usernameField, passwordField, errorLabel));

        // --- Layout ---
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setMaxWidth(360);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 4);");
        card.getChildren().addAll(
                titleLabel,
                subtitleLabel,
                new Separator(),
                usernameLabel,
                usernameField,
                passwordLabel,
                passwordField,
                errorLabel,
                loginButton
        );

        StackPane root = new StackPane(card);
        root.setStyle("-fx-background-color: #ecf0f1;");
        root.setPadding(new Insets(40));

        Scene scene = new Scene(root, 500, 480);

        stage.setTitle("HotelNova - Login");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Handles the login action.
     * On success: logs the event and navigates to MainMenuView.
     * On failure: shows the error message inline without closing the window.
     */
    private void handleLogin(TextField usernameField, PasswordField passwordField, Label errorLabel) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Basic input validation before calling the service
        if (username.isEmpty() || password.isEmpty()) {
            showError(errorLabel, "Por favor complete todos los campos.");
            return;
        }

        try {
            User user = userController.login(username, password);

            // Log the successful login as a simulated HTTP trace
            HttpLogger.post("/api/auth/login", "User '" + username + "' logged in | Role: " + user.getRole());
            AppLogger.info("Successful login: " + username);

            // Navigate to main menu, passing the logged-in user for role-based access
            MainMenuView mainMenu = new MainMenuView(stage, user);
            mainMenu.show();

        } catch (RuntimeException e) {
            // Show the error from UserService without crashing the app
            showError(errorLabel, e.getMessage());
            AppLogger.warn("Failed login attempt for user: " + username);
            HttpLogger.error("POST", "/api/auth/login", "Login failed for: " + username);
        }
    }

    /**
     * Makes the error label visible with the given message.
     */
    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}